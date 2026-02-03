package org.iesalandalus.programacion.tallermecanico.modelo.negocio.mysql;

import org.iesalandalus.programacion.tallermecanico.modelo.TallerMecanicoExcepcion;
import org.iesalandalus.programacion.tallermecanico.modelo.dominio.Cliente;
import org.iesalandalus.programacion.tallermecanico.modelo.negocio.IClientes;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Clientes implements IClientes {

    private Connection conexion;
    private static Clientes instancia;

    private static final String AWS_ENDPOINT = "ec2-35-153-60-227.compute-1.amazonaws.com";
    private static final String AWS_PORT = "3306";
    private static final String AWS_DB_NAME = "tallermecanico";
    private static final String BD_URL = "jdbc:mysql://" + AWS_ENDPOINT + ":" + AWS_PORT + "/" + AWS_DB_NAME;
    private static final String BD_USUARIO = "root";
    private static final String BD_PASSWORD = "ad2526";


    private Clientes() {
    }

    static Clientes getInstancia() {
        if (instancia == null) {
            instancia = new Clientes();
        }
        return instancia;
    }

    @Override
    public void comenzar() {
        try {
            conexion = DriverManager.getConnection(BD_URL, BD_USUARIO, BD_PASSWORD);
        } catch (SQLException e) {
            System.err.println("Error al conectar con la base de datos: " + e.getMessage());
        }
    }

    @Override
    public void terminar() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
                System.out.println("Conexión cerrada.");
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }

    @Override
    public List<Cliente> get() {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT * FROM Clientes";

        try (Statement sentencia = conexion.createStatement();
             ResultSet filas = sentencia.executeQuery(sql)) {

            while (filas.next()) {
                String nombre = filas.getString("Nombre");
                String dni = filas.getString("Dni");
                String telefono = filas.getString("Telefono");
                clientes.add(new Cliente(nombre, dni, telefono));
            }
        } catch (SQLException e) {
            System.err.println("Error al recuperar los clientes: " + e.getMessage());
        } catch (IllegalArgumentException | NullPointerException e) {
            System.err.println("Error en los datos de un cliente: " + e.getMessage());
        }
        return clientes;
    }

    @Override
    public void insertar(Cliente cliente) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(cliente, "No se puede insertar un cliente nulo.");


        if (buscar(cliente) != null) {
            throw new TallerMecanicoExcepcion("Ya existe un cliente con ese DNI.");
        }

        String sql = "INSERT INTO Clientes (Nombre, Dni, Telefono) VALUES (?, ?, ?)";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, cliente.getNombre());
            sentencia.setString(2, cliente.getDni());
            sentencia.setString(3, cliente.getTelefono());
            sentencia.executeUpdate();
        } catch (SQLException e) {
            throw new TallerMecanicoExcepcion("Error al insertar el cliente: " + e.getMessage());
        }
    }

    @Override
    public Cliente modificar(Cliente cliente, String nombre, String telefono) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(cliente, "No se puede modificar un cliente nulo.");


        Cliente clienteEncontrado = buscar(cliente);
        if (clienteEncontrado == null) {
            throw new TallerMecanicoExcepcion("No existe ningún cliente con ese DNI.");
        }


        String nuevoNombre = (nombre != null && !nombre.isBlank()) ? nombre : clienteEncontrado.getNombre();
        String nuevoTelefono = (telefono != null && !telefono.isBlank()) ? telefono : clienteEncontrado.getTelefono();

        String sql = "UPDATE Clientes SET Nombre = ?, Telefono = ? WHERE Dni = ?";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, nuevoNombre);
            sentencia.setString(2, nuevoTelefono);
            sentencia.setString(3, cliente.getDni());
            sentencia.executeUpdate();


            return new Cliente(nuevoNombre, cliente.getDni(), nuevoTelefono);
        } catch (SQLException e) {
            throw new TallerMecanicoExcepcion("Error al modificar el cliente: " + e.getMessage());
        }
    }

    @Override
    public Cliente buscar(Cliente cliente) {
        Objects.requireNonNull(cliente, "No se puede buscar un cliente nulo.");

        String sql = "SELECT * FROM Clientes WHERE Dni = ?";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, cliente.getDni());
            try (ResultSet filas = sentencia.executeQuery()) {
                if (filas.next()) {
                    String nombre = filas.getString("Nombre");
                    String dni = filas.getString("Dni");
                    String telefono = filas.getString("Telefono");
                    return new Cliente(nombre, dni, telefono);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar el cliente: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void borrar(Cliente cliente) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(cliente, "No se puede borrar un cliente nulo.");

        if (buscar(cliente) == null) {
            throw new TallerMecanicoExcepcion("No existe ningún cliente con ese DNI.");
        }

        String sql = "DELETE FROM Clientes WHERE Dni = ?";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, cliente.getDni());
            sentencia.executeUpdate();
        } catch (SQLException e) {
            throw new TallerMecanicoExcepcion("Error al borrar el cliente: " + e.getMessage());
        }
    }
}