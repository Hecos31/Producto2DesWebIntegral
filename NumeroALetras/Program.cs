using System.Text;
using System.Text.Json;
using System.Xml.Linq;

var builder = WebApplication.CreateBuilder(args);
var app = builder.Build();

// Ruta exclusiva para el Paso 2 (ej. http://localhost:5000/paso2/25)
app.MapGet("/paso2/{numero:long}", async (long numero) =>
{
    using var client = new HttpClient();

    string soapEnvelope = $@"<?xml version=""1.0"" encoding=""utf-8""?>
    <soap:Envelope xmlns:soap=""http://schemas.xmlsoap.org/soap/envelope/"">
      <soap:Body>
        <NumberToWords xmlns=""http://www.dataaccess.com/webservicesserver/"">
          <ubiNum>{numero}</ubiNum>
        </NumberToWords>
      </soap:Body>
    </soap:Envelope>";

    var content = new StringContent(soapEnvelope, Encoding.UTF8, "text/xml");
    var soapResponse = await client.PostAsync("https://www.dataaccess.com/webservicesserver/NumberConversion.wso", content);
    var soapResponseString = await soapResponse.Content.ReadAsStringAsync();

    var doc = XDocument.Parse(soapResponseString);
    XNamespace ns = "http://www.dataaccess.com/webservicesserver/";
    var resultadoIngles = doc.Descendants(ns + "NumberToWordsResult").FirstOrDefault()?.Value.Trim() ?? "Error";

    string urlTraduccion = $"https://api.mymemory.translated.net/get?q={Uri.EscapeDataString(resultadoIngles)}&langpair=en|es";
    
    var jsonResponseString = await client.GetStringAsync(urlTraduccion);

    using var jsonDoc = JsonDocument.Parse(jsonResponseString);
    var resultadoEspanol = jsonDoc.RootElement
                                  .GetProperty("responseData")
                                  .GetProperty("translatedText")
                                  .GetString();

    var html = $@"
    <html>
        <head>
            <meta charset=""utf-8"">
            <title>Paso 2 - Traducción .NET 10</title>
        </head>
        <body>
            <h2>Paso 2: Consumo SOAP y Traducción (.NET 10)</h2>
            <p><strong>Número enviado:</strong> {numero}</p>
            <p><strong>Resultado de la función:</strong> {resultadoIngles}</p>
            <p><strong>Resultado final traducido a español:</strong> {resultadoEspanol}</p>
        </body>
    </html>";

    return Results.Content(html, "text/html");
});

app.Run();