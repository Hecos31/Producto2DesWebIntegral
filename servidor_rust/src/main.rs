use actix_web::{get, web, App, HttpResponse, HttpServer, Responder};
use reqwest::Client;

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

#[get("/paso1/{numero}")]
async fn paso1(numero: web::Path<String>) -> impl Responder {
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

    let html = format!(
        r#"<html>
<head>
    <meta charset="utf-8">
    <title>Paso 1 - Consumo SOAP en Rust</title>
</head>
<body>
    <h2>Paso 1: Consumo SOAP (Rust)</h2>
    <p><strong>Número enviado:</strong> {}</p>
    <p><strong>Resultado en inglés:</strong> {}</p>
</body>
</html>"#, num, resultado_ingles);

    HttpResponse::Ok().content_type("text/html; charset=utf-8").body(html)
}

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    println!("Servidor Rust corriendo en http://localhost:8080/paso1/25");
    
    HttpServer::new(|| {
        App::new().service(paso1)
    })
    .bind(("127.0.0.1", 8080))?
    .run()
    .await
}