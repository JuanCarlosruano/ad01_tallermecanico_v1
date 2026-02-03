package org.iesalandalus.programacion.tallermecanico.modelo.negocio.mysql;

import org.iesalandalus.programacion.tallermecanico.modelo.TallerMecanicoExcepcion;
import org.iesalandalus.programacion.tallermecanico.modelo.dominio.Vehiculo;
import org.iesalandalus.programacion.tallermecanico.modelo.negocio.IVehiculos;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Vehiculos implements IVehiculos {

    private Connection conexion;
    private static Vehiculos instancia;

    private static final String AWS_ENDPOINT = "ec2-35-153-60-227.compute-1.amazonaws.com";
    private static final String AWS_PORT = "3306";
    private static final String AWS_DB_NAME = "tallermecanico";
    private static final String BD_URL = "jdbc:mysql://" + AWS_ENDPOINT + ":" + AWS_PORT + "/" + AWS_DB_NAME;
    private static final String BD_USUARIO = "root";
    private static final String BD_PASSWORD = "ad2526";

    private Vehiculos() {
    }

    static Vehiculos getInstancia() {
        if (instancia == null) {
            instancia = new Vehiculos();
        }
        return instancia;
    }

    @Override
    public void comenzar() {
        try {
            conexion = DriverManager.getConnection(BD_URL, BD_USUARIO, BD_PASSWORD);
        } catch (SQLException e) {
            System.err.println("Error al conectar con la base de datos en Vehiculos: " + e.getMessage());
        }
    }

    @Override
    public void terminar() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }

    @Override
    public List<Vehiculo> get() {
        List<Vehiculo> listaVehiculos = new ArrayList<>();
        String sql = "SELECT * FROM Vehiculos";

        try (Statement sentencia = conexion.createStatement();
             ResultSet filas = sentencia.executeQuery(sql)) {

            while (filas.next()) {
                String marca = filas.getString("Marca");
                String modelo = filas.getString("Modelo");
                String matricula = filas.getString("Matricula");
                listaVehiculos.add(new Vehiculo(marca, modelo, matricula));
            }
        } catch (SQLException e) {
            System.err.println("Error al recuperar los vehículos: " + e.getMessage());
        }
        return listaVehiculos;
    }

    @Override
    public void insertar(Vehiculo vehiculo) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(vehiculo, "No se puede insertar un vehículo nulo.");

        if (buscar(vehiculo) != null) {
            throw new TallerMecanicoExcepcion("Ya existe un vehículo con esa matrícula.");
        }

        String sql = "INSERT INTO Vehiculos (Marca, Modelo, Matricula) VALUES (?, ?, ?)";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, vehiculo.marca());
            sentencia.setString(2, vehiculo.modelo());
            sentencia.setString(3, vehiculo.matricula());
            sentencia.executeUpdate();
        } catch (SQLException e) {
            throw new TallerMecanicoExcepcion("Error al insertar el vehículo: " + e.getMessage());
        }
    }

    @Override
    public Vehiculo buscar(Vehiculo vehiculo) {
        Objects.requireNonNull(vehiculo, "No se puede buscar un vehículo nulo.");

        String sql = "SELECT * FROM Vehiculos WHERE Matricula = ?";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, vehiculo.matricula());
            try (ResultSet filas = sentencia.executeQuery()) {
                if (filas.next()) {
                    String marca = filas.getString("Marca");
                    String modelo = filas.getString("Modelo");
                    String matricula = filas.getString("Matricula");
                    return new Vehiculo(marca, modelo, matricula);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar el vehículo: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void borrar(Vehiculo vehiculo) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(vehiculo, "No se puede borrar un vehículo nulo.");

        if (buscar(vehiculo) == null) {
            throw new TallerMecanicoExcepcion("No existe ningún vehículo con esa matrícula.");
        }

        String sql = "DELETE FROM Vehiculos WHERE Matricula = ?";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, vehiculo.matricula());
            sentencia.executeUpdate();
        } catch (SQLException e) {
            throw new TallerMecanicoExcepcion("Error al borrar el vehículo: " + e.getMessage());
        }
    }
}