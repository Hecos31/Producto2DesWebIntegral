#include <iostream>
#include <string>
#define CPPHTTPLIB_OPENSSL_SUPPORT // Fundamental para HTTPS
#include "httplib.h"

using namespace std;

int main() {
    httplib::Server svr;

    svr.Get(R"(/paso1/(\d+))", [](const httplib::Request& req, httplib::Response& res) {
        
        string numero = req.matches[1];
        
        string soap_request = 
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
            "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
            "  <soap:Body>\n"
            "    <NumberToWords xmlns=\"http://www.dataaccess.com/webservicesserver/\">\n"
            "      <ubiNum>" + numero + "</ubiNum>\n"
            "    </NumberToWords>\n"
            "  </soap:Body>\n"
            "</soap:Envelope>";

        httplib::Client cli("https://www.dataaccess.com");
        cli.enable_server_certificate_verification(false);

        httplib::Headers headers = {
            {"SOAPAction", "\"\""}
        };

        auto api_res = cli.Post("/webservicesserver/NumberConversion.wso", headers, soap_request, "text/xml; charset=utf-8");

        string resultado_ingles = "Error: No se pudo conectar al servicio SOAP.";

        if (api_res) {
            if (api_res->status == 200) {
                string tag_inicio = "NumberToWordsResult>";
                string tag_fin = "</"; 
                
                size_t pos_inicio = api_res->body.find(tag_inicio);
                if (pos_inicio != string::npos) {
                    pos_inicio += tag_inicio.length();
                    size_t pos_fin = api_res->body.find(tag_fin, pos_inicio);
                    if (pos_fin != string::npos) {
                        resultado_ingles = api_res->body.substr(pos_inicio, pos_fin - pos_inicio);
                    }
                } else {
                    resultado_ingles = "Fallo de lectura. El servidor respondió esto: " + api_res->body;
                }
            } else {
                resultado_ingles = "Error HTTP " + to_string(api_res->status) + ". El servicio rechazó la petición.";
                cout << "Error del servidor SOAP: " << api_res->body << endl; 
            }
        } else {
            resultado_ingles = "Error de red interno. Código de httplib: " + to_string(static_cast<int>(api_res.error()));
        }

        string html = 
            "<html>\n"
            "  <head>\n"
            "    <meta charset=\"utf-8\">\n"
            "  </head>\n"
            "  <body>\n"
            "    <h2>Consumo de Servicio SOAP Público</h2>\n"
            "    <p><strong>Número enviado:</strong> " + numero + "</p>\n"
            "    <p><strong>Respuesta del servicio:</strong> " + resultado_ingles + "</p>\n"
            "  </body>\n"
            "</html>\n";

        res.set_content(html, "text/html");
    });

    cout << "Servidor C++ corriendo en http://127.0.0.1:4567" << endl;
    svr.listen("127.0.0.1", 4567);

    return 0;
}