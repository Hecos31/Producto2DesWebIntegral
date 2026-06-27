use Mojolicious::Lite -signatures;
use Lingua::Num2Word;
use utf8;

# Ruta exclusiva para el Paso 3 (ej. http://localhost:3000/paso3/25)
get '/paso3/:numero' => sub ($c) {
    my $numero = $c->param('numero');
    
    my $conversor = Lingua::Num2Word->new();
    
    my $resultado_letras = $conversor->cardinal('spa', $numero);
    
    $c->render(inline => <<~'HTML', numero => $numero, letras => ucfirst($resultado_letras));
      <html>
        <head>
          <meta charset="utf-8">
          <title>Paso 3 - Librería Perl</title>
        </head>
        <body>
          <h2>Paso 3: Conversión de Número a Letras (Librería CPAN)</h2>
          <p><strong>Número ingresado:</strong> <%= $numero %></p>
          <p><strong>Resultado con 'Lingua::Num2Word':</strong> <%= $letras %></p>
        </body>
      </html>
    HTML
};

app->start;