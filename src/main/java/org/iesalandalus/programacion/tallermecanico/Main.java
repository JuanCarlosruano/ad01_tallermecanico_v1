package org.iesalandalus.programacion.tallermecanico;

import javafx.util.Pair;
import org.iesalandalus.programacion.tallermecanico.controlador.Controlador;
import org.iesalandalus.programacion.tallermecanico.controlador.IControlador;
import org.iesalandalus.programacion.tallermecanico.modelo.FabricaModelo;
import org.iesalandalus.programacion.tallermecanico.modelo.negocio.FabricaFuenteDatos;
import org.iesalandalus.programacion.tallermecanico.vista.FabricaVista;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {

    private static final String AWS_ENDPOINT = "ec2-35-153-60-227.compute-1.amazonaws.com";
    private static final String BD_URL = "jdbc:mysql://" + AWS_ENDPOINT + ":3306/tallermecanico";
    private static final String BD_USUARIO = "root";
    private static final String BD_PASSWORD = "ad2526";

    public static void main(String[] args) {
        Pair<FabricaVista, FabricaFuenteDatos> fabricas = procesarArgumentos(args);

        System.out.println("Iniciando aplicación con fuente de datos: " + fabricas.getValue());

        IControlador controlador = new Controlador(FabricaModelo.CASCADA, fabricas.getValue(), fabricas.getKey());
        controlador.comenzar();
    }

    private static Pair<FabricaVista, FabricaFuenteDatos> procesarArgumentos(String[] args) {
        FabricaVista fabricaVista = FabricaVista.VENTANAS;
        FabricaFuenteDatos fabricaFuenteDatos = FabricaFuenteDatos.FICHEROS_JSON;

        for (String argumento : args) {
            if (argumento.equalsIgnoreCase("-vventanas")) {
                fabricaVista = FabricaVista.VENTANAS;
            } else if (argumento.equalsIgnoreCase("-vtexto")) {
                fabricaVista = FabricaVista.TEXTO;
            } else if (argumento.equalsIgnoreCase("-fdficherosxml")) {
                fabricaFuenteDatos = FabricaFuenteDatos.FICHEROS_XML;
            } else if (argumento.equalsIgnoreCase("-fdficherosjson")) {
                fabricaFuenteDatos = FabricaFuenteDatos.FICHEROS_JSON;
            } else if (argumento.equalsIgnoreCase("-fdmysql")) {
                fabricaFuenteDatos = FabricaFuenteDatos.MYSQL;
            }
        }

        if (fabricaFuenteDatos == FabricaFuenteDatos.MYSQL) {
            if (!comprobarConexionMySQL()) {
                System.err.println("¡No se pudo establecer conexión con la Base de Datos MySQL.");
                fabricaFuenteDatos = FabricaFuenteDatos.FICHEROS_JSON;
            } else {
                System.out.println("Conexión con MySQL verificada correctamente.");
            }
        }

        return new Pair<>(fabricaVista, fabricaFuenteDatos);
    }

    private static boolean comprobarConexionMySQL() {
        try (Connection conexion = DriverManager.getConnection(BD_URL, BD_USUARIO, BD_PASSWORD)) {
            return true;
        } catch (SQLException e) {
            System.err.println("Error de conexión detectado: " + e.getMessage());
            return false;
        }
    }
}