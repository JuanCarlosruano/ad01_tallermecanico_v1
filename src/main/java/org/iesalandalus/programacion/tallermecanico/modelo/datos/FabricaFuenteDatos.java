package org.iesalandalus.programacion.tallermecanico.modelo.datos;


import org.iesalandalus.programacion.tallermecanico.modelo.datos.ficheros.FuenteDatosFicherosJSON;

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
    };

    public abstract IFuenteDatos crear();
}
