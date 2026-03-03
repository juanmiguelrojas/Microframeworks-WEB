# TDSE MicroFramework (Guía rápida)

Microframework HTTP en Java, simple y didáctico.  
Objetivo: publicar servicios GET con lambda y servir archivos estáticos, sin patrones tipo Spring Boot.

## 1) Qué clases importan y cómo se conectan

- `HttpServer` (clase principal):
  - Expone la mini API: `get(...)`, `staticfiles(...)`, `start()`.
  - Atiende sockets HTTP y decide si responder REST o archivo estático.
- `utilities/URLParser`:
  - Parsea la URL entrante (path + query params).
  - `HttpServer` lo usa para construir `req.getValues("...")`.
- `utilities/EchoServer` y `utilities/EchoClient`:
  - Son base conceptual de sockets TCP (`ServerSocket`/`Socket`) usada luego en `HttpServer`.
- `utilities/URLReader`:
  - Ejemplo simple de lectura desde URL/stream.

## 2) Flujo de una petición (mental model)

1. Llega una petición a `HttpServer`.
2. `HttpServer` parsea la URI con `URLParser`.
3. Si hay ruta registrada con `get("/ruta", lambda)`, ejecuta la lambda.
4. Si no hay ruta REST, intenta servir archivo estático desde `staticfiles(...)`.
5. Si nada coincide, responde `404`.

## 3) Requerimientos cubiertos

- **GET con lambda**
  - `HttpServer.get("/hello", (req, res) -> "Hello " + req.getValues("name"));`
- **Query params**
  - `req.getValues("name")` toma valores de `?name=...`.
- **Static files**
  - `HttpServer.staticfiles("/webroot")` busca en `src/main/resources/webroot`.

## 4) Ejemplo mínimo (ya en `HttpServer.main`)

```java
public static void main(String[] args) throws Exception {
    staticfiles("/webroot");

    get("/hello", (req, res) -> "Hello " + req.getValues("name"));
    get("/pi", (req, res) -> String.valueOf(Math.PI));

    start();
}
```

## 5) Cómo correr y probar

```bash
mvn clean test
mvn clean package
java -cp target/classes com.eci.microframework.HttpServer
```

Probar en navegador:

- `http://localhost:8080/hello?name=Pedro`
- `http://localhost:8080/pi`
- `http://localhost:8080/index.html`

## 6) Estructura final esperada

- `src/main/java/com/eci/microframework/HttpServer.java`
- `src/main/java/com/eci/microframework/utilities/URLParser.java`
- `src/main/java/com/eci/microframework/utilities/EchoServer.java`
- `src/main/java/com/eci/microframework/utilities/EchoClient.java`
- `src/main/java/com/eci/microframework/utilities/URLReader.java`
