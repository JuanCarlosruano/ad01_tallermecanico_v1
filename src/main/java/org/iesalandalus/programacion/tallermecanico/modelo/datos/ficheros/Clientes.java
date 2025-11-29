package org.iesalandalus.programacion.tallermecanico.modelo.datos.ficheros;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.iesalandalus.programacion.tallermecanico.modelo.TallerMecanicoExcepcion;
import org.iesalandalus.programacion.tallermecanico.modelo.dominio.Cliente;
import org.iesalandalus.programacion.tallermecanico.modelo.datos.IClientes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Clientes implements IClientes {


    private static final String FICHERO_CLIENTES = "datos/ficheros/json/clientes.json";

    private static Clientes instancia;
    private final ObjectMapper mapper;


    private Clientes() {
        mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);


        File fichero = new File(FICHERO_CLIENTES);
        if (!fichero.getParentFile().exists()) {
            fichero.getParentFile().mkdirs();
        }
    }

    static Clientes getInstancia() {
        if (instancia == null) {
            instancia = new Clientes();
        }
        return instancia;
    }

    @Override
    public void comenzar() {

    }

    @Override
    public void terminar() {

    }


    public List<Cliente> leer() {
        File fichero = new File(FICHERO_CLIENTES);
        if (!fichero.exists()) {
            return new ArrayList<>();
        }
        try {
            return mapper.readValue(fichero, new TypeReference<List<Cliente>>() {});
        } catch (IOException e) {
            System.out.printf("Error al leer el fichero JSON: %s%n", e.getMessage());
            return new ArrayList<>();
        }
    }


    public void escribir(List<Cliente> clientes) {
        Objects.requireNonNull(clientes, "La lista de clientes no puede ser nula.");
        try {
            mapper.writeValue(new File(FICHERO_CLIENTES), clientes);
        } catch (IOException e) {
            System.out.printf("Error al escribir en el fichero JSON: %s%n", e.getMessage());
        }
    }

    @Override
    public List<Cliente> get() {
        return leer();
    }

    @Override
    public void insertar(Cliente cliente) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(cliente, "No se puede insertar un cliente nulo.");

        List<Cliente> clientes = leer();

        if (clientes.contains(cliente)) {
            throw new TallerMecanicoExcepcion("Ya existe un cliente con ese DNI.");
        }

        clientes.add(cliente);
        escribir(clientes);
    }

    @Override
    public Cliente modificar(Cliente cliente, String nombre, String telefono) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(cliente, "No se puede modificar un cliente nulo.");

        List<Cliente> clientes = leer();
        int indice = clientes.indexOf(cliente);

        if (indice == -1) {
            throw new TallerMecanicoExcepcion("No existe ningún cliente con ese DNI.");
        }

        Cliente clienteEncontrado = clientes.get(indice);

        if (nombre != null && !nombre.isBlank()) {
            clienteEncontrado.setNombre(nombre);
        }
        if (telefono != null && !telefono.isBlank()) {
            clienteEncontrado.setTelefono(telefono);
        }

        clientes.set(indice, clienteEncontrado);
        escribir(clientes);

        return clienteEncontrado;
    }

    @Override
    public Cliente buscar(Cliente cliente) {
        Objects.requireNonNull(cliente, "No se puede buscar un cliente nulo.");

        List<Cliente> clientes = leer();
        int indice = clientes.indexOf(cliente);

        return (indice == -1) ? null : clientes.get(indice);
    }

    @Override
    public void borrar(Cliente cliente) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(cliente, "No se puede borrar un cliente nulo.");

        List<Cliente> clientes = leer();

        if (!clientes.contains(cliente)) {
            throw new TallerMecanicoExcepcion("No existe ningún cliente con ese DNI.");
        }

        clientes.remove(cliente);
        escribir(clientes);
    }
}