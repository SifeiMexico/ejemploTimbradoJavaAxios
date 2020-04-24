# ![Sifei](https://www.sifei.com.mx/web/image/res.company/1/logo?unique=38c7250)




# Ejemplos de timbrado y cancelación en JAVA

Este repositorio incluye en ejemplos de los servicios SOAP de timbrado y cancelación de Sifei en en lenguaje JAVA.

# Pasos

Para utilizar este proyecto debes importar el JAR que se encuentra en la siguiente liga:

https://github.com/SifeiMexico/timbradoJarAxis2

O simplemente [Descargar JAR aquí](https://github.com/SifeiMexico/timbradoJarAxis2/raw/master/target/mx.com.sifei-1.0.0.jar)

El Jar descargado contiene los artefactos para consumir el WS,  este proyecto ya las incluye por lo que no debes realizar 

## Importar en maven

Si utilizas maven como gestor de dependencias deberas instalar el Jar en un repositorio local, para eso debes primero descargar el JAR y colocar su ruta en el siguiente comando:
```shell
mvn install:install-file –Dfile="pathDelJar" -DgroupId=mx.com.sifei -DartifactId=timbrado -Dversion=1.0.0
```
Un repositorio local es un directorio en el equipo donde se ejecuta Maven.
Con esto maven debera reconocer el Jar en el repositorio interno definido en el archivo pom

```xml
<repositories>
       <repository>
                <id>local-maven-repo</id>
            <url>file:///${project.basedir}/local-maven-repo</url>
        </repository>        
</repositories>
```

## Configuración de ejemplos



Los ejemplos se alimentan de un archivo config.ini para leer los datos de conexion, **No hacer esto en produccion**.
La url esta configurada al entorno de pruebas.

Para ejecutar estas pruebas debes solicitar tus accesos de QA(pruebas).

http://sifei.com.mx/

```ini
[timbrado]
UsuarioSIFEI = RFC # usuario sifei
PasswordSIFEI = 12345678a #password de usuario de sifei 
IdEquipoGenerado = f1563ce5 # ide equipo

[cancelacion]
PFX = CER_KEY.pfx    #Solo para el servicio de cancelacion


```
### Rutas
Los ejemplos ademas utilizan rutas relativas, por lo que deberas cambiarlas por las rutas de tus archivos de prueba.

## Metodos con ejemplos

WS           | Método           |Descripción
------------ | -----------------|-------------
Timbrado     | `getCFDI()`      |Metodo para timbrar CFDI
Cancelación  | `cancelaCFDI()`  | Metodo para cancelar CFDI

## ¿Necesitas mas ejemplos?
Si necesitas mas ejemplos del resto de servicios favor de generar un nuevo issue