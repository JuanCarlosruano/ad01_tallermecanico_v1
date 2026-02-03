package org.iesalandalus.programacion.tallermecanico.modelo.negocio;


import org.iesalandalus.programacion.tallermecanico.modelo.negocio.json.FuenteDatosFicherosJSON;
import org.iesalandalus.programacion.tallermecanico.modelo.negocio.mysql.FuenteDatosFicherosMySQL;

public enum FabricaFuenteDatos {

    FICHEROS_XML {
        @Override
        public IFuenteDatos crear() {
            return new FuenteDatosFicherosJSON();
        }
    },

    FICHEROS_JSON {
        @Override
        public IFuenteDatos crear() {
            return new FuenteDatosFicherosJSON();
        }
    },
    MYSQL {
        @Override
        public IFuenteDatos crear() {
            return new FuenteDatosFicherosMySQL();
        }
    };

    public abstract IFuenteDatos crear();
}
