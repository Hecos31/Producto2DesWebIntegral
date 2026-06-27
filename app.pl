use Mojolicious::Lite -signatures;
use SOAP::Lite;

sub consumir_soap ($numero) {
    my $client = SOAP::Lite
        ->proxy('https://www.dataaccess.com/webservicesserver/NumberConversion.wso')
        ->uri('http://www.dataaccess.com/webservicesserver/');

    # Construimos la petición con los namespaces correctos
    my $method = SOAP::Data->name('NumberToWords')
        ->attr({xmlns => 'http://www.dataaccess.com/webservicesserver/'});
    my $param = SOAP::Data->name('ubiNum')->value($numero);

    my $response = $client->call($method => $param);

    if ($response->fault) {
        return "Error SOAP: " . $response->faultstring;
    }

    my $resultado = $response->result;
    $resultado =~ s/^\s+|\s+$//g if defined $resultado;
    
    return $resultado;
}

# Ruta que recibe el número en la URL (ej. http://localhost:3000/paso1/25)
get '/paso1/:numero' => sub ($c) {
    my $numero = $c->param('numero');
    
    my $resultado_ingles = consumir_soap($numero);
    
    $c->render(inline => <<~'HTML', numero => $numero, resultado => $resultado_ingles);
      <html>
        <head>
          <meta charset="utf-8">
          <title>Paso 1 - SOAP Perl</title>
        </head>
        <body>
          <h2>Paso 1: Consumo SOAP (Perl)</h2>
          <p><strong>Número enviado:</strong> <%= $numero %></p>
          <p><strong>Resultado en inglés:</strong> <%= $resultado %></p>
        </body>
      </html>
    HTML
};

app->start;