 
package dhf.mx.com.sifei.usinaxis;


import cancelacion.sifei.service.CancelaCFDIDocument;
import cancelacion.sifei.service.CancelaCFDIResponse;
import cancelacion.sifei.service.CancelaCFDIResponseDocument;
import java.io.ByteArrayInputStream;
import java.rmi.RemoteException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import mapeados.GetCFDI;
import mapeados.GetCFDIDocument;
import mapeados.impl.GetCFDIImpl;
import mx.com.sifei.cancelacion.CancelacionStub;
import mx.com.sifei.timbrado.SIFEIServiceStub;
import mx.com.sifei.timbrado.SifeiExceptionException;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.ini4j.Wini;

/**
 * Ejemplo de timbrado PHP del WS SOAP getCFDI() de SIFEI utiliznado axis2
 * 
 * El CFDI(XML) debe estar sellado correctamente Nota: Para
 *         simplificar los ejemplos todas las rutas son relativas y los datos se
 *         leen de un archivo config.ini, lo cual no debe de hacerse en un
 *         ambiente de produccion.
 * 
 *         Este ejemplo usa axis2
 * 
 * 
 */
public class ClienteTimbradoSIFEI {
    
    public static void main (String[] args) throws IOException, XMLStreamException{
       cancelar(); 
    }
    
 public static void cancelar() throws AxisFault, IOException, XMLStreamException{
            //leyendo datos , cambiar origen de datos
            Wini ini = new Wini(new File("./config.ini"));
            String UsuarioSIFEI = ini.get("timbrado", "UsuarioSIFEI", String.class);
            String PasswordSIFEI = ini.get("timbrado", "PasswordSIFEI", String.class);
            String IdEquipo=ini.get("timbrado", "IdEquipoGenerado", String.class);            
            String pfxPath=ini.get("timbrado", "PFX", String.class);
            String pfxPass=ini.get("timbrado", "PFXPass", String.class);
            
            //se lee el PFX
            byte[] pfxBytes=Files.readAllBytes(Paths.get(pfxPath));            
            
            
            List<String> uuids = new ArrayList();      
            uuids.add("8b2d26b2-f96b-410e-aaeb-664d2af28794");//uuid a cancelar
            
            CancelacionStub cancelacionStub=new CancelacionStub();  
            CancelaCFDIDocument cancela= CancelaCFDIDocument.Factory.newInstance();
            var cancelaParams=cancela.addNewCancelaCFDI();
            
            //llenado de parametros
            cancelaParams.setUsuarioSIFEI(UsuarioSIFEI);    
            cancelaParams.setPasswordSifei(PasswordSIFEI);
            cancelaParams.setPfx(pfxBytes);
            cancelaParams.setPasswordPfx(pfxPass);
            cancelaParams.setRfcEmisor("XAXX010101000");
            cancelaParams.setUuidsArray(uuids.stream().toArray(String[]::new));
            try{
               CancelaCFDIResponseDocument responseDocument= cancelacionStub.cancelaCFDI(cancela);
               CancelaCFDIResponse response=responseDocument.getCancelaCFDIResponse();
               
               //obtenemos el acuse de cancelacion
                String xmlAcuseCancelacion= response.getReturn();
                 
                response.setReturn(xmlAcuseCancelacion); 
                //se imprime acuse, se puede guardar en bd,etc.
                 System.out.println(xmlAcuseCancelacion);
                
            }catch(Exception e){
                 System.out.println("\n:::::EXCEPCION:::::");
                e.printStackTrace();
                 
                
            }
            finally{
                
                System.out.println("\n:::::SOAP REQUEST:::::");
                cancelacionStub._getServiceClient().getLastOperationContext().getMessageContext("Out").getEnvelope().serialize(System.out);

                 
            }
     
    
 }
 
    
    public static void timbrar() throws AxisFault, XMLStreamException, IOException {

        SIFEIServiceStub serviceStub = null;
       
        try {
            //para efectos de demostracion los acceso se leen desde un archivo de config.
            Wini ini = new Wini(new File("./config.ini"));

            String usuarioSIFEI = ini.get("timbrado", "UsuarioSIFEI", String.class);
            String passwordSIFEI = ini.get("timbrado", "PasswordSIFEI", String.class);
            String idEquipo=ini.get("timbrado", "IdEquipoGenerado", String.class);//Id de equipo proporcionado por correo electrónico             
            String serie = "";
            
            
            
            //Esta es la url del ambiente de pruebas
            String urlTimbrado = "http://devcfdi.sifei.com.mx:8080/SIFEI33/SIFEI?wsdl";
            

            serviceStub = new SIFEIServiceStub( urlTimbrado );
           
            var document= GetCFDIDocument.Factory.newInstance();
            var getCFDI=document.addNewGetCFDI();
            //Llenado de parametros obligatorios que tienen que enviarse
            
            byte[] bFile = Files.readAllBytes(Path.of("./assets/xml_sellado.xml")); //cambia la ruta por una que exista en tu sistema de archivos.
            getCFDI.setArchivoXMLZip(bFile);
            getCFDI.setIdEquipo(idEquipo);
            getCFDI.setPassword(passwordSIFEI);
            getCFDI.setSerie(serie);
            getCFDI.setUsuario(usuarioSIFEI);      
            //se invoca al metodo
            var getCFDIResponseE = serviceStub.getCFDI(document);
            //se extrae la respuesta
            var getCFDIResponse = getCFDIResponseE.getGetCFDIResponse();
            

            //La respuesta es un zip, se obtienen los bytes del zip que contiene el XML para luego extraer el xml del zip.
            String XMLTimbrado = getFileFromZipBytes(getCFDIResponse.getReturn());
            System.out.println("\n"+XMLTimbrado);

        } catch ( SifeiExceptionException ex) {
            //Captura del Resquest
            System.out.println("\n:::::EXCEPCION:::::");
            ex.printStackTrace();

        }finally {
            System.out.println("\n:::::SOAP REQUEST:::::");
            serviceStub._getServiceClient().getLastOperationContext().getMessageContext("Out").getEnvelope().serialize(System.out);

            System.out.println("\n:::::SOAP RESPONSE:::::");
            serviceStub._getServiceClient().getLastOperationContext().getMessageContext("In").getEnvelope().serialize(System.out);
        
        }
        
    }
    
    public void format(OutputStream os){
        var xml=new String(os.toString());
       System.out.println(  xml.replaceAll("--><","-->\n<"));
    }
public static String getFileFromZipBytes(byte[] returns) throws FileNotFoundException, IOException {
        //se vierten los bytes dentro de un inputStream
        InputStream inputStream = new ByteArrayInputStream(returns);

        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        ZipEntry entry = null;
        //se itera dentro de cada elemento
        
        while ((entry = zipInputStream.getNextEntry()) != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int b = zipInputStream.read();
            while (b >= 0) {
                baos.write(b);
                b = zipInputStream.read();
            }
            zipInputStream.close();
            return new String(baos.toByteArray());
            
        }
        zipInputStream.close();
        return null;
    }
    public static String getFileFromZipDataHandler(DataHandler dataHandler) throws FileNotFoundException, IOException {

        InputStream inputStream = dataHandler.getInputStream();

        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        ZipEntry entry = null;

        while ((entry = zipInputStream.getNextEntry()) != null) {


            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int b = zipInputStream.read();
            while (b >= 0) {
                baos.write(b);
                b = zipInputStream.read();
            }
            zipInputStream.close();
            return new String(baos.toByteArray());
            
        }
        zipInputStream.close();
        return null;
    }
    
}
