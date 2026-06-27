package main

import (
	"bytes"
	"encoding/xml"
	"fmt"
	"io"
	"net/http"
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

func handlerPaso1(w http.ResponseWriter, r *http.Request) {
	numero := r.PathValue("numero")

	soapReq := fmt.Sprintf(`<?xml version="1.0" encoding="utf-8"?>
    <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
      <soap:Body>
        <NumberToWords xmlns="http://www.dataaccess.com/webservicesserver/">
          <ubiNum>%s</ubiNum>
        </NumberToWords>
      </soap:Body>
    </soap:Envelope>`, numero)

	resp, err := http.Post("https://www.dataaccess.com/webservicesserver/NumberConversion.wso",
		"text/xml; charset=utf-8",
		bytes.NewBufferString(soapReq))

	if err != nil {
		http.Error(w, "Error consumiendo SOAP: "+err.Error(), http.StatusInternalServerError)
		return
	}
	defer resp.Body.Close()

	bodyBytes, _ := io.ReadAll(resp.Body)
	var envelope Envelope
	xml.Unmarshal(bodyBytes, &envelope)

	resultadoIngles := strings.TrimSpace(envelope.Body.Response.Result)
	if resultadoIngles == "" {
		resultadoIngles = "Error en SOAP"
	}

	html := fmt.Sprintf(`
    <html>
        <head>
            <meta charset="utf-8">
            <title>Paso 1 - SOAP Golang</title>
        </head>
        <body>
            <h2>Paso 1: Consumo SOAP (Golang)</h2>
            <p><strong>Número enviado:</strong> %s</p>
            <p><strong>Resultado en inglés:</strong> %s</p>
        </body>
    </html>`, numero, resultadoIngles)

	w.Header().Set("Content-Type", "text/html; charset=utf-8")
	w.Write([]byte(html))
}

func main() {
	http.HandleFunc("GET /paso1/{numero}", handlerPaso1)

	fmt.Println("Servidor Golang levantado. Entra a http://localhost:8080/paso1/25")
	
	if err := http.ListenAndServe(":8080", nil); err != nil {
		fmt.Println("Error al iniciar el servidor:", err)
	}
}