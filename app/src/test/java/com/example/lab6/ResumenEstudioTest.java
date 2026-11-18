package com.example.lab6;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class ResumenEstudioTest {

    private SesionEstudio registro(String curso, String fecha, double horas, String tipo) {
        SesionEstudio sesion = new SesionEstudio();
        sesion.setCurso(curso);
        sesion.setFecha(fecha);
        sesion.setHoras(horas);
        sesion.setTipoActividad(tipo);
        return sesion;
    }

    @Test
    public void sumaHorasPorMesYActividad() {
        ResumenEstudio resumen = new ResumenEstudio(Arrays.asList(
                registro("Redes", "2026-11-03", 2, "Teoría"),
                registro("Redes", "2026-11-04", 1, "Práctica"),
                registro("IoT", "2026-10-28", 1, "Laboratorio")), null);

        assertEquals(4, resumen.getTotalHoras(), 0.0001);
        assertEquals(1, resumen.getHorasPorMes().get("2026-10"), 0.0001);
        assertEquals(3, resumen.getHorasPorMes().get("2026-11"), 0.0001);
        assertEquals(2, resumen.getHorasPorActividad().get("Teoría"), 0.0001);
        assertEquals(1, resumen.getHorasPorActividad().get("Práctica"), 0.0001);
        assertEquals(1, resumen.getHorasPorActividad().get("Laboratorio"), 0.0001);
    }

    @Test
    public void filtraPorCursoYConservaDecimales() {
        ResumenEstudio resumen = new ResumenEstudio(Arrays.asList(
                registro(" Redes ", "2026-11-03", 1.5, "Teoría"),
                registro("REDES", "2026-11-04", 0.5, "Práctica"),
                registro("IoT", "2026-11-05", 3, "Laboratorio")), "redes");

        assertEquals(2, resumen.getTotalHoras(), 0.0001);
        assertEquals(0, resumen.getHorasPorActividad().get("Laboratorio"), 0.0001);
        assertEquals(75, 100 * resumen.getHorasPorActividad().get("Teoría") / resumen.getTotalHoras(), 0.0001);
    }

    @Test
    public void mantieneSeparadosLosAniosYOrdenaLosMeses() {
        ResumenEstudio resumen = new ResumenEstudio(Arrays.asList(
                registro("IoT", "2026-01-02", 3, "Teoría"),
                registro("IoT", "2025-12-20", 1, "Teoría"),
                registro("IoT", "2025-01-02", 2, "Teoría")), null);

        assertEquals(Arrays.asList("2025-01", "2025-12", "2026-01"),
                new ArrayList<>(resumen.getHorasPorMes().keySet()));
        assertEquals(2, resumen.getHorasPorMes().get("2025-01"), 0.0001);
        assertEquals(3, resumen.getHorasPorMes().get("2026-01"), 0.0001);
    }

    @Test
    public void manejaFiltroSinResultados() {
        ResumenEstudio resumen = new ResumenEstudio(Collections.singletonList(
                registro("IoT", "2026-11-03", 2, "Teoría")), "Redes");
        assertEquals(0, resumen.getTotalHoras(), 0.0001);
        assertTrue(resumen.getHorasPorMes().isEmpty());
    }

    @Test
    public void validaFechasHorasYTipos() {
        assertTrue(SesionEstudio.fechaValida("2024-02-29"));
        assertFalse(SesionEstudio.fechaValida("2026-02-29"));
        assertFalse(SesionEstudio.fechaValida("2026-11-31"));
        assertFalse(SesionEstudio.fechaValida("2026-1-03"));
        assertFalse(SesionEstudio.fechaValida("2026-11-03extra"));
        assertFalse(registro("IoT", "2026-11-03", 0, "Teoría").esValida());
        assertFalse(registro("IoT", "2026-11-03", Double.NaN, "Teoría").esValida());
        assertFalse(registro("IoT", "2026-11-03", Double.POSITIVE_INFINITY, "Teoría").esValida());
        assertFalse(registro(" ", "2026-11-03", 2, "Teoría").esValida());
        assertFalse(registro("IoT", "2026-11-03", 2, "Otro").esValida());
    }

    @Test
    public void guardaSoloLosCuatroCamposDelRegistro() {
        SesionEstudio sesion = registro(" Redes ", "2026-11-03", 1.5, "Práctica");
        sesion.setId("documento-original");
        assertEquals(4, sesion.aMapa().size());
        assertEquals("Redes", sesion.aMapa().get("curso"));
        assertEquals(1.5, sesion.aMapa().get("horas"));
        assertFalse(sesion.aMapa().containsKey("id"));
    }
}
