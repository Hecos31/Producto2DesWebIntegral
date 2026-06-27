using System.Text;
using System.Xml.Linq;

var builder = WebApplication.CreateBuilder(args);
var app = builder.Build();

// Ruta que recibe el número en la URL (ej. http://localhost:5000/paso1/25)
app.MapGet("/paso1/{numero:long}", async (long numero) =>
{
    string soapEnvelope = $@"<?xml version=""1.0"" encoding=""utf-8""?>
    <soap:Envelope xmlns:soap=""http://schemas.xmlsoap.org/soap/envelope/"">
      <soap:Body>
        <NumberToWords xmlns=""http://www.dataaccess.com/webservicesserver/"">
          <ubiNum>{numero}</ubiNum>
        </NumberToWords>
      </soap:Body>
    </soap:Envelope>";

    using var client = new HttpClient();
    var content = new StringContent(soapEnvelope, Encoding.UTF8, "text/xml");
    
    var response = await client.PostAsync("https://www.dataaccess.com/webservicesserver/NumberConversion.wso", content);
    var responseString = await response.Content.ReadAsStringAsync();

    var doc = XDocument.Parse(responseString);
    XNamespace ns = "http://www.dataaccess.com/webservicesserver/";
    
    var resultadoIngles = doc.Descendants(ns + "NumberToWordsResult").FirstOrDefault()?.Value.Trim() ?? "Error en SOAP";

    var html = $@"
    <html>
        <head>
            <meta charset=""utf-8"">
            <title>Paso 1 - SOAP .NET 10</title>
        </head>
        <body>
            <h2>Paso 1: Consumo SOAP (.NET 10)</h2>
            <p><strong>Número enviado:</strong> {numero}</p>
            <p><strong>Resultado en inglés:</strong> {resultadoIngles}</p>
        </body>
    </html>";

    return Results.Content(html, "text/html");
});

app.Run();