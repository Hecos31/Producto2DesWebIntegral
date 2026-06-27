require 'sinatra'
require 'savon'
require 'net/http'
require 'uri'
require 'json'
require 'humanize'

# Ruta que recibe el número en la URL (ej. http://localhost:4567/paso2/25)
get '/paso3/:numero' do
  numero = params[:numero].to_i
  resultado_letras = numero.humanize(locale: :es)
  
  <<~HTML
    <html>
      <head>
        <meta charset="utf-8">
        <title>Paso 3 - Usando Librería</title>
      </head>
      <body>
        <h2>Paso 3: Conversión de Número a Letras</h2>
        <p><strong>Número ingresado:</strong> #{numero}</p>
        <p><strong>Resultado con 'humanize':</strong> #{resultado_letras.capitalize}</p>
      </body>
    </html>
  HTML
end
