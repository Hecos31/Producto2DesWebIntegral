use actix_web::{get, web, App, HttpResponse, HttpServer, Responder};
use reqwest::Client;

#[get("/paso3/{numero}")]
async fn paso3(numero: web::Path<String>) -> impl Responder {
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

    let mut resultado_ingles = "Error en SOAP".to_string();
    if let Ok(res) = soap_res {
        if let Ok(body) = res.text().await {
            if let Some(pos) = body.find("<m:NumberToWordsResult>") {
                let start = pos + 23;
                if let Some(end) = body[start..].find("</m:NumberToWordsResult>") {
                    resultado_ingles = body[start..start + end].to_string();
                }
            }
        }
    }

    let resultado_espanol = match num.as_str() {
        "25" => "veinticinco",
        "10" => "diez",
        _ => "número no mapeado en ejemplo",
    };

    let html = format!(
        r#"<html><body>
            <h2>Paso 3: Conversión Final</h2>
            <p>Inglés: {}</p>
            <p><strong>Español: {}</strong></p>
        </body></html>"#, resultado_ingles, resultado_espanol);

    HttpResponse::Ok().content_type("text/html; charset=utf-8").body(html)
}

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    HttpServer::new(|| App::new().service(paso3))
        .bind(("127.0.0.1", 8080))?.run().await
}