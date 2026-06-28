use actix_web::{get, web, App, HttpResponse, HttpServer, Responder};
use reqwest::Client;
use serde_json::Value;

fn extraer_xml(xml: &str, etiqueta: &str) -> String {
    let tag_inicio = format!("<{}>", etiqueta);
    let tag_fin = format!("</{}>", etiqueta);
    
    if let Some(inicio) = xml.find(&tag_inicio) {
        let pos_inicio = inicio + tag_inicio.len();
        if let Some(fin) = xml[pos_inicio..].find(&tag_fin) {
            return xml[pos_inicio..pos_inicio + fin].trim().to_string();
        }
    }
    "Error: No se encontró la etiqueta".to_string()
}

#[get("/paso2/{numero}")]
async fn paso2(numero: web::Path<String>) -> impl Responder {
    let num = numero.into_inner();
    let client = Client::new();

    let soap_envelope = format!(
        r#"<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <NumberToWords xmlns="http://www.dataaccess.com/webservicesserver/">
      <ubiNum>{}</ubiNum>
    </NumberToWords>
  </soap:Body>
</soap:Envelope>"#, num);

    let soap_res = client.post("https://www.dataaccess.com/webservicesserver/NumberConversion.wso")
        .header("Content-Type", "text/xml; charset=utf-8")
        .header("SOAPAction", "\"\"")
        .body(soap_envelope)
        .send()
        .await;

    let mut resultado_ingles = String::new();

    match soap_res {
        Ok(res) => {
            if let Ok(body) = res.text().await {
                resultado_ingles = extraer_xml(&body, "m:NumberToWordsResult");
            }
        },
        Err(e) => {
            resultado_ingles = format!("Error de conexión SOAP: {}", e);
        }
    }

    let texto_query = resultado_ingles.replace(" ", "%20");
    let url_traduccion = format!("https://api.mymemory.translated.net/get?q={}&langpair=en|es", texto_query);
    
    let trans_res = client.get(&url_traduccion).send().await;
    let mut resultado_espanol = String::new();

    match trans_res {
        Ok(res) => {
            if let Ok(json) = res.json::<Value>().await {
                if let Some(texto_traducido) = json["responseData"]["translatedText"].as_str() {
                    resultado_espanol = texto_traducido.to_string();
                }
            }
        },
        Err(e) => {
            resultado_espanol = format!("Error en traducción: {}", e);
        }
    }

    let html = format!(
        r#"<html>
<head>
    <meta charset="utf-8">
    <title>Paso 2 - Rust Actix</title>
</head>
<body>
    <h2>Paso 2: SOAP y Traducción (Rust)</h2>
    <p><strong>Número enviado:</strong> {}</p>
    <p><strong>Resultado en inglés:</strong> {}</p>
    <p><strong>Resultado final traducido a español:</strong> {}</p>
</body>
</html>"#, num, resultado_ingles, resultado_espanol);

    HttpResponse::Ok().content_type("text/html; charset=utf-8").body(html)
}

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    println!("Servidor Rust corriendo en http://localhost:8080/paso2/25");
    
    HttpServer::new(|| {
        App::new().service(paso2)
    })
    .bind(("127.0.0.1", 8080))?
    .run()
    .await
}