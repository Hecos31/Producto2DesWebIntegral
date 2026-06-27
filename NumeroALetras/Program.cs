using System.Globalization;
using Humanizer;

var builder = WebApplication.CreateBuilder(args);
var app = builder.Build();

// Ruta exclusiva para el Paso 3 (ej. http://localhost:5000/paso3/2145321)
app.MapGet("/paso3/{numero:long}", (long numero) =>
{
    string resultadoLetras = numero.ToWords(new CultureInfo("es-MX"));
    
    if (!string.IsNullOrEmpty(resultadoLetras))
    {
        resultadoLetras = char.ToUpper(resultadoLetras[0]) + resultadoLetras.Substring(1);
    }

    var html = $@"
    <html>
        <head>
            <meta charset=""utf-8"">
            <title>Paso 3 - .NET 10 NuGet</title>
        </head>
        <body>
            <h2>Paso 3: Conversión de Número a Letras (Librería Humanizer)</h2>
            <p><strong>Número ingresado:</strong> {numero}</p>
            <p><strong>Resultado con 'Humanizer':</strong> {resultadoLetras}</p>
        </body>
    </html>";

    return Results.Content(html, "text/html");
});

app.Run();