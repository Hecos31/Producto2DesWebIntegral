#include <iostream>
#include <string>
#include <vector>
#include "httplib.h"

using namespace std;

string numeroALetras(int n) {
    if (n == 0) return "cero";
    if (n < 0) return "menos " + numeroALetras(-n);

    string resultado = "";

    if (n >= 1000000) {
        int millones = n / 1000000;
        int resto = n % 1000000;
        if (millones == 1) {
            resultado = "un millón";
        } else {
            resultado = numeroALetras(millones) + " millones";
        }
        if (resto > 0) resultado += " " + numeroALetras(resto);
        return resultado;
    }

    // Lógica para Miles
    if (n >= 1000) {
        int miles = n / 1000;
        int resto = n % 1000;
        if (miles == 1) {
            resultado = "mil";
        } else {
            string m = numeroALetras(miles);
            if (m.size() >= 3 && m.substr(m.size() - 3) == "uno") {
                m = m.substr(0, m.size() - 1); 
            } else if (m == "uno") {
                m = "un";
            }
            resultado = m + " mil";
        }
        if (resto > 0) resultado += " " + numeroALetras(resto);
        return resultado;
    }

    if (n >= 100) {
        if (n == 100) return "cien";
        vector<string> centenas = {"", "ciento", "doscientos", "trescientos", "cuatrocientos", "quinientos", "seiscientos", "setecientos", "ochocientos", "novecientos"};
        resultado = centenas[n / 100];
        int resto = n % 100;
        if (resto > 0) resultado += " " + numeroALetras(resto);
        return resultado;
    }

    if (n >= 30) {
        vector<string> decenas = {"", "", "", "treinta", "cuarenta", "cincuenta", "sesenta", "setenta", "ochenta", "noventa"};
        resultado = decenas[n / 10];
        int resto = n % 10;
        if (resto > 0) resultado += " y " + numeroALetras(resto);
        return resultado;
    }

    vector<string> unidades = {
        "cero", "uno", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve", "diez", 
        "once", "doce", "trece", "catorce", "quince", "dieciséis", "diecisiete", "dieciocho", "diecinueve", 
        "veinte", "veintiuno", "veintidós", "veintitrés", "veinticuatro", "veinticinco", "veintiséis", 
        "veintisiete", "veintiocho", "veintinueve"
    };
    return unidades[n];
}

int main() {
    httplib::Server svr;

    svr.Get(R"(/paso1/(\d+))", [](const httplib::Request& req, httplib::Response& res) {
        
        // 1. Extraer el número de la URL y convertirlo a Entero (int)
        string numero_str = req.matches[1];
        int numero_int = stoi(numero_str);
        
        // 2. Usar nuestra función nativa de C++
        string resultado_espanol = numeroALetras(numero_int);

        // 3. Renderizar el HTML
        string html = 
            "<html>\n"
            "  <head>\n"
            "    <meta charset=\"utf-8\">\n"
            "  </head>\n"
            "  <body>\n"
            "    <h2>Conversión de Números a Letras (C++ Nativo)</h2>\n"
            "    <p><strong>Número recibido:</strong> " + numero_str + "</p>\n"
            "    <p><strong>Resultado:</strong> <span style='color: #d9534f; font-size: 1.2em; text-transform: capitalize;'>" + resultado_espanol + "</span></p>\n"
            "  </body>\n"
            "</html>\n";

        res.set_content(html, "text/html");
    });

    cout << "Servidor C++ nativo corriendo en http://127.0.0.1:4567" << endl;
    
    // Arrancar el servidor
    svr.listen("127.0.0.1", 4567);

    return 0;
}