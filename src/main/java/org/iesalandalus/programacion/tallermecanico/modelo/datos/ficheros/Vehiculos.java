package org.iesalandalus.programacion.tallermecanico.modelo.datos.ficheros;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.iesalandalus.programacion.tallermecanico.modelo.TallerMecanicoExcepcion;
import org.iesalandalus.programacion.tallermecanico.modelo.dominio.Cliente;
import org.iesalandalus.programacion.tallermecanico.modelo.dominio.Vehiculo;
import org.iesalandalus.programacion.tallermecanico.modelo.datos.IVehiculos;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Vehiculos implements IVehiculos {


    private static final String FICHERO_VEHICULOS = "datos/ficheros/json/vehiculos.json";
    private ObjectMapper mapper;

    private static Vehiculos instancia;

    private Vehiculos() {
        mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        File fichero = new File(FICHERO_VEHICULOS);
        if (!fichero.getParentFile().exists()) {
            fichero.getParentFile().mkdirs();
        }
    }


    static Vehiculos getInstancia() {
        if (instancia == null) {
        }
        return instancia;
    }

    @Override
    public void comenzar() {

    }




    @Override
    public void terminar() {

    }

    public List<Vehiculo> leer() {
        File fichero = new File(FICHERO_VEHICULOS);
        if (!fichero.exists()) {
            return new ArrayList<>();
        }
        try {
            return mapper.readValue(fichero, new TypeReference<List<Vehiculo>>() {});
        } catch (IOException e) {
            System.out.printf("Error al leer el fichero JSON: %s%n", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Vehiculo> get() {
        return leer();
    }

    public void escribir(List<Vehiculo> vehiculos) {
        Objects.requireNonNull(vehiculos, "La lista de clientes no puede ser nula.");
        try {
            mapper.writeValue(new File(FICHERO_VEHICULOS), vehiculos);
        } catch (IOException e) {
            System.out.printf("Error al escribir en el fichero JSON: %s%n", e.getMessage());
        }
    }

    @Override
    public void insertar(Vehiculo vehiculo) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(vehiculo, "No se puede insertar un cliente nulo.");

        List<Vehiculo> vehiculos = leer();

        if (vehiculos.contains(vehiculo)) {
            throw new TallerMecanicoExcepcion("Ya existe un cliente con ese DNI.");
        }

        vehiculos.add(vehiculo);
        escribir(vehiculos);
    }

    @Override
    public Vehiculo buscar(Vehiculo vehiculo) {
        Objects.requireNonNull(vehiculo, "No se puede buscar un cliente nulo.");

        List<Vehiculo> vehiculos = leer();
        int indice = vehiculos.indexOf(vehiculo);

        return (indice == -1) ? null : vehiculos.get(indice);
    }

    @Override
    public void borrar(Vehiculo vehiculo) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(vehiculo, "No se puede borrar un cliente nulo.");

        List<Vehiculo> vehiculos = leer();

        if (!vehiculos.contains(vehiculo)) {
            throw new TallerMecanicoExcepcion("No existe ningún cliente con ese DNI.");
        }

        vehiculos.remove(vehiculo);
        escribir(vehiculos);
    }
}
