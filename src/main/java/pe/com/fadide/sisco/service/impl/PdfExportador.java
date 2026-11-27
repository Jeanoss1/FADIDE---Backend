package pe.com.fadide.sisco.service.impl;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import pe.com.fadide.sisco.dto.TablaReporte;
import pe.com.fadide.sisco.service.ExportadorReporte;

/** Estrategia PDF: A4 horizontal con encabezado de marca, tabla con filas alternadas y totales. */
@Component
public class PdfExportador implements ExportadorReporte {

    private static final Color OSCURO = new Color(0x1F, 0x1F, 0x1F);
    private static final Color NARANJA = new Color(0xF7, 0x81, 0x1E);
    private static final Color ALTERNO = new Color(0xF4, 0xF5, 0xF7);
    private static final Color GRIS = new Color(0x6B, 0x72, 0x80);
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public String formato() {
        return "pdf";
    }

    @Override
    public MediaType tipoContenido() {
        return MediaType.APPLICATION_PDF;
    }

    @Override
    public String extension() {
        return "pdf";
    }

    @Override
    public byte[] exportar(TablaReporte reporte) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4.rotate(), 30, 30, 30, 30);
        PdfWriter.getInstance(documento, salida);
        documento.open();

        Font marca = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, NARANJA);
        Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, OSCURO);
        Font detalle = FontFactory.getFont(FontFactory.HELVETICA, 9, GRIS);

        documento.add(new Paragraph("FADIDE Ingeniería y Construcción E.I.R.L. · SISCO", marca));
        documento.add(new Paragraph(reporte.titulo(), titulo));
        documento.add(new Paragraph(reporte.filtros(), detalle));
        documento.add(new Paragraph("Generado el " + LocalDateTime.now().format(FECHA_HORA)
                + " · " + reporte.filas().size() + " registro(s)", detalle));
        documento.add(new Paragraph(" "));

        documento.add(tabla(reporte));
        documento.close();
        return salida.toByteArray();
    }

    private PdfPTable tabla(TablaReporte reporte) {
        List<String> columnas = reporte.columnas();
        PdfPTable tabla = new PdfPTable(columnas.size());
        tabla.setWidthPercentage(100);
        tabla.setHeaderRows(1); // el encabezado se repite en cada página

        Font cabecera = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
        Font celda = FontFactory.getFont(FontFactory.HELVETICA, 8, OSCURO);
        Font total = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, OSCURO);

        for (String columna : columnas) {
            PdfPCell c = new PdfPCell(new Phrase(columna, cabecera));
            c.setBackgroundColor(OSCURO);
            c.setPadding(5);
            c.setBorderColor(OSCURO);
            tabla.addCell(c);
        }

        if (reporte.filas().isEmpty()) {
            PdfPCell vacio = new PdfPCell(new Phrase("No hay registros para los filtros seleccionados", celda));
            vacio.setColspan(columnas.size());
            vacio.setPadding(8);
            vacio.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabla.addCell(vacio);
            return tabla;
        }

        int i = 0;
        for (List<String> fila : reporte.filas()) {
            Color fondo = (i++ % 2 == 1) ? ALTERNO : Color.WHITE;
            agregarFila(tabla, reporte, fila, celda, fondo);
        }
        if (reporte.totales() != null) {
            agregarFila(tabla, reporte, reporte.totales(), total, new Color(0xFF, 0xF3, 0xE8));
        }
        return tabla;
    }

    private void agregarFila(PdfPTable tabla, TablaReporte reporte, List<String> valores, Font fuente, Color fondo) {
        for (int col = 0; col < valores.size(); col++) {
            PdfPCell c = new PdfPCell(new Phrase(valores.get(col) == null ? "" : valores.get(col), fuente));
            c.setBackgroundColor(fondo);
            c.setPadding(4);
            c.setBorderColor(new Color(0xE5, 0xE7, 0xEB));
            if (reporte.numericas().contains(col)) {
                c.setHorizontalAlignment(Element.ALIGN_RIGHT);
            }
            tabla.addCell(c);
        }
    }
}
