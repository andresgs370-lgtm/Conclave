# Conclave

Conclave es una aplicación web para generar contenido multimedia mediante proveedores externos de inteligencia artificial. El proyecto está dividido en un frontend desarrollado con Angular y un backend desarrollado con Spring Boot.

## Contenido

- [Tecnologías](#tecnologías)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Funcionalidades](#funcionalidades)
- [Proveedores de generación](#proveedores-de-generación)
- [Seguridad y roles](#seguridad-y-roles)
- [API REST](#api-rest)
- [Requisitos](#requisitos)
- [Configuración](#configuración)
- [Ejecución](#ejecución)
- [Documentación de la API](#documentación-de-la-api)
- [Consideraciones del estado actual](#consideraciones-del-estado-actual)

## Tecnologías

### Frontend

- Angular 22
- TypeScript
- RxJS
- Bootstrap 5.3.3
- Animate.css

### Backend

- Java 21
- Spring Boot 4.1.2
- Spring Web MVC
- Spring Data JPA
- Spring Security
- Spring Validation
- Spring Mail
- Springdoc OpenAPI 2.8.9
- ModelMapper
- Gson
- JJWT 0.12.6
- MySQL Connector/J
- H2 (dependencia de ejecución incluida en el proyecto)

## Estructura del proyecto

El repositorio contiene dos aplicaciones independientes:

```text
.
├── conclaveBack/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/co/edu/unbosque/conclave/
│   │   │   │   ├── configuration/
│   │   │   │   ├── controller/
│   │   │   │   ├── dto/
│   │   │   │   ├── exception/
│   │   │   │   ├── model/
│   │   │   │   ├── repository/
│   │   │   │   ├── security/
│   │   │   │   ├── service/
│   │   │   │   └── util/
│   │   │   └── resources/
│   │   └── test/
│   ├── mvnw
│   ├── mvnw.cmd
│   └── pom.xml
│
└── front/
    ├── public/
    ├── src/
    │   └── app/
    │       ├── component/
    │       └── services/
    ├── angular.json
    └── package.json
```

### Backend

El backend organiza sus responsabilidades en los siguientes paquetes:

- `configuration`: configuración de ejecución, carga inicial de datos y documentación OpenAPI.
- `controller`: endpoints HTTP de la aplicación.
- `dto`: objetos utilizados para recibir y devolver información.
- `exception`: excepciones y manejo global de errores.
- `model`: entidades y enumeraciones del dominio.
- `repository`: acceso a los datos mediante repositorios.
- `security`: configuración de Spring Security y autenticación JWT.
- `service`: lógica de negocio, autenticación por correo, gestión de API keys y comunicación con proveedores.
- `util`: utilidades, incluido el cifrado AES.

### Frontend

La aplicación Angular contiene componentes para las páginas y elementos de interfaz, además de servicios para la comunicación con el backend, la autenticación y la gestión de las generaciones.

Entre sus componentes se encuentran las páginas de inicio, modelos, inicio de sesión, registro, verificación, recuperación de contraseña, generación de imágenes, videos y sonido, perfil y administración.

## Funcionalidades

Según la implementación incluida en el proyecto, Conclave cuenta con las siguientes funcionalidades:

- Registro de usuarios.
- Inicio de sesión.
- Verificación de cuenta mediante un código enviado por correo.
- Reenvío del código de verificación.
- Solicitud y confirmación del restablecimiento de contraseña.
- Consulta del perfil del usuario autenticado.
- Generación de imágenes.
- Generación de videos.
- Generación de audio.
- Consulta del historial de prompts.
- Consulta administrativa del historial.
- Consulta y actualización de las API keys de proveedores desde el área de administración.
- Gestión administrativa de usuarios.
- Manejo centralizado de errores de la API.

La generación se solicita mediante el endpoint `POST /prompt/generar`. El servicio del frontend mantiene en memoria el estado de la última generación de cada tipo mientras la aplicación permanece abierta. El código del servicio indica que los resultados multimedia de esa sesión no se conservan en la base de datos del navegador y se pierden al recargar la página.

## Proveedores de generación

El backend declara los siguientes proveedores en `AIProvider`:

| Proveedor | Uso declarado en el proyecto |
|---|---|
| OpenRouter | Imágenes |
| Google Gemini | Imágenes y sonido |
| Cloudflare Workers AI | Imágenes y sonido |
| Hugging Face – MiniMax H3 FlashGen | Video |
| Hugging Face – MiniMax H3 Turbo | Video |
| Hugging Face – Omni Video | Video |
| Hugging Face – ModelScope Text-to-Video | Video |
| Pollinations.ai | Imágenes; la API key es opcional |
| Together AI | Imágenes |
| ElevenLabs | Sonido |

Los proveedores disponibles dependen del tipo de contenido solicitado y de la configuración de sus credenciales. El backend también permite administrar las API keys desde los endpoints administrativos correspondientes.

## Seguridad y roles

La seguridad del backend está implementada con Spring Security y tokens JWT.

- La API utiliza una configuración sin estado (`STATELESS`).
- Las contraseñas se codifican mediante BCrypt.
- Las rutas de autenticación bajo `/auth/**` son públicas.
- La documentación Swagger está configurada como pública.
- `/user/me`, `/prompt/**` y `/history/me` permiten acceso a usuarios con rol `USER` o `ADMIN`.
- Las rutas `/user/**`, `/admin/**` y `/history/**` requieren el rol `ADMIN`, con la excepción específica de `/user/me` y `/history/me` indicada en las reglas anteriores.
- Las demás rutas requieren autenticación.

El frontend guarda el token de sesión en `localStorage` bajo la clave `conclave_token` y lo envía en el encabezado `Authorization` con el esquema `Bearer`.

## API REST

Los siguientes endpoints corresponden a los mappings declarados en los controladores del backend.

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/auth/login` | Iniciar sesión. |
| `POST` | `/auth/register` | Registrar una cuenta. |
| `POST` | `/auth/reenviar-codigo` | Reenviar el código de verificación. |
| `POST` | `/auth/verificar-registro` | Verificar el registro. |
| `POST` | `/auth/solicitar-reset` | Solicitar el restablecimiento de contraseña. |
| `POST` | `/auth/confirmar-reset` | Confirmar el restablecimiento de contraseña. |
| `GET` | `/user/me` | Consultar el perfil del usuario autenticado. |
| `POST` | `/user/createjson` | Crear un usuario mediante JSON. |
| `GET` | `/user/getall` | Consultar todos los usuarios. |
| `GET` | `/user/count` | Consultar la cantidad de usuarios. |
| `GET` | `/user/exists/{id}` | Comprobar si existe un usuario por ID. |
| `GET` | `/user/getbyid/{id}` | Consultar un usuario por ID. |
| `PUT` | `/user/updatejson` | Actualizar un usuario mediante JSON. |
| `PUT` | `/user/setenabled/{id}` | Cambiar el estado habilitado de un usuario. |
| `DELETE` | `/user/deletebyid/{id}` | Eliminar un usuario por ID. |
| `POST` | `/prompt/generar` | Solicitar una generación de contenido. |
| `GET` | `/history/me` | Consultar el historial del usuario autenticado. |
| `GET` | `/history` | Consultar el historial completo. |
| `GET` | `/history/user/{id}` | Consultar el historial de un usuario por ID. |
| `GET` | `/admin/apikey/getall` | Consultar las API keys registradas, enmascaradas. |
| `PUT` | `/admin/apikey/update/{provider}` | Actualizar la API key y el estado de un proveedor. |
| `PUT` | `/admin/apikey/setenabled/{provider}` | Cambiar el estado habilitado de un proveedor. |

Los endpoints administrativos están protegidos por rol `ADMIN`, de acuerdo con la configuración de seguridad del backend.

## Requisitos

Para trabajar con el proyecto se necesita:

- Java 21.
- Node.js y npm compatibles con la versión de Angular utilizada por el proyecto.
- Una base de datos compatible con la configuración de Spring Data JPA. El proyecto incluye el controlador JDBC de MySQL.
- Credenciales de correo para las funciones de verificación y recuperación de contraseña.
- Las credenciales de los proveedores externos que se quieran utilizar.

El backend incluye Maven Wrapper (`mvnw` y `mvnw.cmd`), por lo que no es necesario instalar Maven globalmente para ejecutar los comandos mediante el wrapper.

## Configuración

### Backend

El archivo de configuración se encuentra en:

```text
conclaveBack/src/main/resources/application.properties
```

En la copia del proyecto utilizada para este README, ese archivo está vacío. Por tanto, antes de ejecutar el backend se deben definir allí los parámetros de conexión a la base de datos y los valores de configuración que requiera el entorno.

El código de `EmailService` requiere la propiedad:

```properties
app.mail.from=REMITENTE_CONFIGURADO
```

También se utiliza la propiedad `jwt.secret`. El código declara un valor predeterminado para esta propiedad, pero para un despliegue propio se debe configurar una clave secreta adecuada.

Las API keys de los proveedores se gestionan desde la funcionalidad administrativa de la aplicación. No se incluyen credenciales reales en este README.

### Frontend

El servicio `Apiservice` tiene configurada directamente esta URL base:

```text
https://gpcueb.org/conclave-0.0.1-SNAPSHOT
```

Por ello, el frontend apunta a esa dirección en su estado actual. Para utilizar un backend local o una instalación diferente, se debe cambiar la URL base en:

```text
front/src/app/services/apiservice.ts
```

El backend configura CORS para los siguientes orígenes:

- `http://localhost:8080`
- `http://localhost:8081`
- `http://localhost:5173`
- `http://localhost:4200`
- `https://conclaveia.netlify.app`

Si se utiliza otro origen para el frontend, deberá ajustarse la configuración CORS del backend.

## Ejecución

Los comandos siguientes se ejecutan desde las carpetas indicadas.

### 1. Backend

Entrar en la carpeta:

```bash
cd conclaveBack
```

En Windows, ejecutar:

```powershell
.\mvnw.cmd spring-boot:run
```

En Linux o macOS, ejecutar:

```bash
./mvnw spring-boot:run
```

Para generar el artefacto:

```bash
./mvnw clean package
```

En Windows:

```powershell
.\mvnw.cmd clean package
```

Antes de iniciar el backend, configurar `application.properties` con los datos del entorno. La copia del proyecto contiene ese archivo vacío, por lo que no se puede asumir que el backend arrancará sin configuración adicional.

### 2. Frontend

En otra terminal, entrar en la carpeta:

```bash
cd front
```

Instalar las dependencias:

```bash
npm install
```

Iniciar el servidor de desarrollo:

```bash
npm start
```

El script `start` ejecuta `ng serve`. La configuración de Angular establece el modo de desarrollo como configuración predeterminada para este servidor.

Para generar la compilación de producción:

```bash
npm run build
```

Para ejecutar las pruebas configuradas:

```bash
npm test
```

## Documentación de la API

El backend incluye Springdoc OpenAPI y una configuración personalizada de la documentación.

Con el backend en ejecución, las rutas habituales de la interfaz y el documento OpenAPI son:

```text
/swagger-ui/index.html
/v3/api-docs
```

La configuración de seguridad permite el acceso público a las rutas de Swagger y OpenAPI.

## Consideraciones del estado actual

- El archivo `application.properties` incluido en el proyecto no contiene configuración.
- La URL base del backend está escrita directamente en el servicio Angular `Apiservice`.
- El proyecto incluye dependencias para MySQL y H2; la configuración efectiva de la base de datos depende de los parámetros que se definan en el entorno.
- Las API keys de proveedores externos no se documentan aquí ni se deben publicar en el repositorio.
- El estado de las generaciones en el frontend se conserva en memoria durante la sesión de la aplicación; el servicio indica que ese estado se pierde al recargar la página.

