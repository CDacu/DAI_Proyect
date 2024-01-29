# DAI - HYBRIDSERVER

Este es el trabajo de la asignatura de Desarrollo de Aplicaciones para Internet

## Errores sin Solucionar

### General
* Hay flujos sin cerrar en Launcher y en ServiceThread.
* Los tests no funcionan por haber movido clases base de paquete.
* Existen ResultSet no cerrados.
* Los listados de HTML no identifican a qué servidor pertenece cada enlace y los enlaces remotos no apuntan directamente a sus servidores.

### XSD & XSLT
* La validación de un XML con el XSD se hace construyendo un árbol DOM, lo cual es ineficiente e innecesario.

### P2P / WebServices
* Los servicios web no usan el mismo thread pool que HTTP, por lo que no hay control en el número máximo de peticiones activas.
* Los listados no identifican a qué servidor pertenece cada enlace y los enlaces remotos no apuntan directamente a sus servidores.

### Configuración
* Cuando hay un error en el fichero de configuración la aplicación finaliza lanzando una excepción no controlada. Debería finalizar correctamente mostrando un mensaje de error al usuario.
