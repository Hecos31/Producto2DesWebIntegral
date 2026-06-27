package main

import (
	"bytes"
	"encoding/json"
	"encoding/xml"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"strings"
)

type Envelope struct {
	Body Body `xml:"Body"`
}
type Body struct {
	Response NumberToWordsResponse `xml:"NumberToWordsResponse"`
}
type NumberToWordsResponse struct {
	Result string `xml:"NumberToWordsResult"`
}

type TranslationResponse struct {
	ResponseData struct {
		TranslatedText string `json:"translatedText"`
	} `json:"responseData"`
}

func handlerPaso2(w http.ResponseWriter, r *http.Request) {
	numero := r.PathValue("numero")

	soapReq := fmt.Sprintf(`<?xml version="1.0" encoding="utf-8"?>
    <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
      <soap:Body>
        <NumberToWords xmlns="http://www.dataaccess.com/webservicesserver/">
          <ubiNum>%s</ubiNum>
        </NumberToWords>
      </soap:Body>
    </soap:Envelope>`, numero)

	respSOAP, err := http.Post("https://www.dataaccess.com/webservicesserver/NumberConversion.wso",
		"text/xml; charset=utf-8",
		bytes.NewBufferString(soapReq))

	if err != nil {
		http.Error(w, "Error consumiendo SOAP: "+err.Error(), http.StatusInternalServerError)
		return
	}
	defer respSOAP.Body.Close()

	bodySOAP, _ := io.ReadAll(respSOAP.Body)
	var envelope Envelope
	xml.Unmarshal(bodySOAP, &envelope)

	resultadoIngles := strings.TrimSpace(envelope.Body.Response.Result)
	if resultadoIngles == "" {
		resultadoIngles = "Error"
	}

	urlTraduccion := fmt.Sprintf("https://api.mymemory.translated.net/get?q=%s&langpair=en|es", url.QueryEscape(resultadoIngles))
	
	respJSON, err := http.Get(urlTraduccion)
	if err != nil {
		http.Error(w, "Error consumiendo API de traducción: "+err.Error(), http.StatusInternalServerError)
		return
	}
	defer respJSON.Body.Close()

	bodyJSON, _ := io.ReadAll(respJSON.Body)
	var transResp TranslationResponse
	
	json.Unmarshal(bodyJSON, &transResp)
	resultadoEspanol := transResp.ResponseData.TranslatedText

	html := fmt.Sprintf(`
    <html>
        <head>
            <meta charset="utf-8">
            <title>Paso 2 - Traducción Golang</title>
        </head>
        <body>
            <h2>Paso 2: Consumo SOAP y Traducción (Golang)</h2>
            <p><strong>Número enviado:</strong> %s</p>
            <p><strong>Resultado de la función (Paso 1):</strong> %s</p>
            <p><strong>Resultado final traducido a español:</strong> %s</p>
        </body>
    </html>`, numero, resultadoIngles, resultadoEspanol)

	w.Header().Set("Content-Type", "text/html; charset=utf-8")
	w.Write([]byte(html))
}

func main() {
	http.HandleFunc("GET /paso2/{numero}", handlerPaso2)

	fmt.Println("Servidor Golang listo para el Paso 2. Entra a http://localhost:8080/paso2/25")
	
	if err := http.ListenAndServe(":8080", nil); err != nil {
		fmt.Println("Error al iniciar el servidor:", err)
	}
}