package es.uvigo.esei.dai.hybridserver.dao;

import es.uvigo.esei.dai.hybridserver.configuration.Configuration;
import es.uvigo.esei.dai.hybridserver.configuration.ServerConfiguration;
import es.uvigo.esei.dai.hybridserver.exception.DatabaseOfflineException;
import es.uvigo.esei.dai.hybridserver.exception.PageNotFoundException;
import es.uvigo.esei.dai.hybridserver.http.HTTPResourceName;
import es.uvigo.esei.dai.hybridserver.webservice.HybridServerService;
import jakarta.xml.ws.Service;

import javax.xml.namespace.QName;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class DaoDBServerController implements DaoDBController {

    AbstractDaoDB page;
    Configuration configuration;

    public DaoDBServerController(AbstractDaoDB page, Configuration configuration) {
        this.page = page;
        this.configuration = configuration;
    }

    @Override
    public String get(String uuid, HTTPResourceName table) throws PageNotFoundException, DatabaseOfflineException {
        String content = null;
        try{
            content = page.get(uuid, table);
        }catch (Exception e){
            // Si no se encuentra se deja a null y es busca en otra
        }

        if(content != null){
            return content;

        }else if(!configuration.getServers().isEmpty()){
            try{
                for (ServerConfiguration server : configuration.getServers()){
                    try{
                        URL url = new URL(server.getWsdl());
                        QName qname = new QName(server.getNamespace(), server.getService());
                        Service service = Service.create(url, qname);
                        HybridServerService pagesWebService = service.getPort(HybridServerService.class);

                        content = pagesWebService.get(uuid, table);

                        if(content != null){return content;}

                    }catch (PageNotFoundException e){
                        // No se hace nada, simplemente se busca en la siguiente
                    } catch (Exception e){
                        System.err.println("WARNING: An error ocurred while connecting to the remote server "+server.getName());
                    }
                }
            }catch (Exception e){
                //Por si acaso, es un try con recursos
            }
        }
        throw new PageNotFoundException();
    }

    @Override
    public List<String> list(HTTPResourceName table) throws DatabaseOfflineException {

        List<String> toRet = page.list(table);

        if(!configuration.getServers().isEmpty()){
            try{
                for (ServerConfiguration server : configuration.getServers()){
                    try{
                        URL url = new URL(server.getWsdl());
                        QName namespace = new QName(server.getNamespace(), server.getService());
                        Service service = Service.create(url, namespace);
                        HybridServerService pagesWebService = service.getPort(HybridServerService.class);

                        toRet.addAll(Arrays.asList(pagesWebService.list(table)));

                    }catch (Exception e){
                        System.err.println("WARNING: An error ocurred while connecting to the remote server "+server.getName());
                        e.printStackTrace();
                    }
                }
            }catch (Exception e){
                //Por si acaso, es un try con recursos
            }
        }
        return toRet;
    }

    @Override
    public void delete(String uuid, HTTPResourceName table) throws PageNotFoundException, DatabaseOfflineException {
        page.delete(uuid, table);
    }

    @Override
    public String create(String content, HTTPResourceName table) throws DatabaseOfflineException {
        return page.create(content, table);
    }

    @Override
    public String create(String xsd, String content, HTTPResourceName table) throws DatabaseOfflineException {
        return page.create(xsd, content, table);
    }

    @Override
    public String getXSDUUIDwithXSD(String uuid, HTTPResourceName table) throws PageNotFoundException, DatabaseOfflineException {
        return page.getXSDUUIDwithXSD(uuid, table);
    }

    public String getXSDUUIDwithXSLT(String uuid, HTTPResourceName table) throws PageNotFoundException, DatabaseOfflineException {
        String xsd = null;
        try{
            xsd = page.getXSDUUIDwithXSLT(uuid, table);
        }catch (Exception e){
            // Si no se encuentra se deja a null y es busca en otra
        }

        if(xsd != null){
            return xsd;

        }else if(!configuration.getServers().isEmpty()){
            try {
                for (ServerConfiguration server : configuration.getServers()) {
                    try {
                        URL url = new URL(server.getWsdl());
                        QName namespace = new QName(server.getNamespace(), server.getService());
                        Service service = Service.create(url, namespace);
                        HybridServerService pagesWebService = service.getPort(HybridServerService.class);

                        xsd = pagesWebService.getXSD(uuid);

                        if (xsd != null) {return xsd;}

                    } catch (PageNotFoundException e){
                        // No se hace nada, simplemente se busca en la siguiente
                    }
                    catch (Exception e) {
                        System.err.println("WARNING: An error ocurred while connecting to the remote server " + server.getName());
                    }
                }
            } catch (Exception e) {
                //Por si acaso, es un try con recursos
            }
        }
        throw new PageNotFoundException();
    }
}
