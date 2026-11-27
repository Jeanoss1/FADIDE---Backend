package pe.com.fadide.sisco.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import pe.com.fadide.sisco.dto.TablaReporte;
import pe.com.fadide.sisco.service.ExportadorReporte;

/** Estrategia Excel (.xlsx): título, filtros, encabezado con autofiltro y fila de totales. */
@Component
public class ExcelExportador implements ExportadorReporte {

    private static final int FILA_ENCABEZADO = 3;

    @Override
    public String formato() {
        return "excel";
    }

    @Override
    public MediaType tipoContenido() {
        return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @Override
    public String extension() {
        return "xlsx";
    }

    @Override
    public byte[] exportar(TablaReporte reporte) {
        try (Workbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Sheet hoja = libro.createSheet("Reporte");
            int columnas = reporte.columnas().size();

            Font negrita = libro.createFont();
            negrita.setBold(true);
            Font blanca = libro.createFont();
            blanca.setBold(true);
            blanca.setColor(IndexedColors.WHITE.getIndex());

            CellStyle estiloTitulo = libro.createCellStyle();
            Font grande = libro.createFont();
            grande.setBold(true);
            grande.setFontHeightInPoints((short) 14);
            estiloTitulo.setFont(grande);

            CellStyle estiloCabecera = libro.createCellStyle();
            estiloCabecera.setFont(blanca);
            estiloCabecera.setFillForegroundColor(IndexedColors.GREY_80_PERCENT.getIndex());
            estiloCabecera.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            bordes(estiloCabecera);

            CellStyle estiloTexto = libro.createCellStyle();
            bordes(estiloTexto);
            CellStyle estiloNumero = libro.createCellStyle();
            bordes(estiloNumero);
            estiloNumero.setAlignment(HorizontalAlignment.RIGHT);

            CellStyle estiloTotal = libro.createCellStyle();
            estiloTotal.setFont(negrita);
            estiloTotal.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
            estiloTotal.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            estiloTotal.setAlignment(HorizontalAlignment.RIGHT);
            bordes(estiloTotal);

            celda(hoja.createRow(0), 0, reporte.titulo(), estiloTitulo);
            celda(hoja.createRow(1), 0, reporte.filtros(), null);
            hoja.addMergedRegion(new CellRangeAddress(1, 1, 0, Math.max(columnas - 1, 0)));

            Row cabecera = hoja.createRow(FILA_ENCABEZADO);
            for (int c = 0; c < columnas; c++) {
                celda(cabecera, c, reporte.columnas().get(c), estiloCabecera);
            }

            int r = FILA_ENCABEZADO + 1;
            for (List<String> fila : reporte.filas()) {
                Row row = hoja.createRow(r++);
                for (int c = 0; c < fila.size(); c++) {
                    celda(row, c, fila.get(c), reporte.numericas().contains(c) ? estiloNumero : estiloTexto);
                }
            }
            if (reporte.totales() != null && !reporte.filas().isEmpty()) {
                Row row = hoja.createRow(r);
                for (int c = 0; c < reporte.totales().size(); c++) {
                    celda(row, c, reporte.totales().get(c), estiloTotal);
                }
            }

            if (!reporte.filas().isEmpty()) {
                hoja.setAutoFilter(new CellRangeAddress(FILA_ENCABEZADO, FILA_ENCABEZADO + reporte.filas().size(), 0, columnas - 1));
            }
            hoja.createFreezePane(0, FILA_ENCABEZADO + 1);
            for (int c = 0; c < columnas; c++) {
                hoja.autoSizeColumn(c);
            }

            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo generar el archivo Excel", e);
        }
    }

    private static void celda(Row fila, int columna, String valor, CellStyle estilo) {
        Cell cell = fila.createCell(columna);
        cell.setCellValue(valor == null ? "" : valor);
        if (estilo != null) {
            cell.setCellStyle(estilo);
        }
    }

    private static void bordes(CellStyle estilo) {
        estilo.setBorderBottom(BorderStyle.THIN);
        estilo.setBorderTop(BorderStyle.THIN);
        estilo.setBorderLeft(BorderStyle.THIN);
        estilo.setBorderRight(BorderStyle.THIN);
    }
}
