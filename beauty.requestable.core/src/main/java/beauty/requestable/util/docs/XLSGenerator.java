package beauty.requestable.util.docs;

import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import beauty.requestable.util.date.DateFormat;
import beauty.requestable.util.date.DateTime;
import beauty.requestable.util.reflection.UtilReflection;

public class XLSGenerator {
	private static final int MAX_ROWS = 65534;
	private static final int MAX_ROW_WIDTH_PX = 1534;
	private String reportName = "Reporte Excel";
	private boolean showDescriptor = false;
	private HashMap<String, String> reportParameters; // <parameterName,parameterValue>
	private List<TableHeader> headers = new ArrayList<TableHeader>();
	private List<?> values;
	private int rownum = 0;
	private int progress = 0;
	Workbook workbook;

	public void setValues(List<?> values) {
		this.values = values;
	}

	public void putHeader(String fieldName, String headerName) {
		headers.add(new TableHeader(fieldName, headerName));
	}

	public List<TableHeader> getHeaders() {
		return headers;
	}

	public void genera(OutputStream out) throws Exception {
		// Blank workbook
		workbook = new HSSFWorkbook();
		// Create a blank sheet
		Sheet sheet = workbook.createSheet(reportName);

		CellStyle cellStyle = workbook.createCellStyle();
		// cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
		cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

		Row row;

		// Create the descriptor for the report: Name, Date, Parameters (with value)
		if (showDescriptor) {
			createDescriptor(sheet, reportName, reportParameters);
			rownum++;
		}

		row = sheet.createRow(rownum);
		// Create report headers
		createHeaders(sheet,row, headers, 0);

		/*for (int i = 0; i < headers.size(); i++) {
			TableHeader h = headers.get(i);
			if (h.getColumnWidth() > 0) {
				sheet.setColumnWidth(i, calculateColWidth(h.getColumnWidth()));
			} else {
				sheet.autoSizeColumn(i, true);
			}
		}*/

		/*
		 * int width =
		 * sheet.getColumnWidth(2)*headers.get(1).getHeaderName().length()/headers.get(2
		 * ).getHeaderName().length(); sheet.setColumnWidth(1,width);
		 */
		int index = 0;
		progress = 0;
		for (Object obj : values) {
			if (rownum + 1 > MAX_ROWS)
				break;
			row = sheet.createRow(++rownum);
			createData(obj, sheet, cellStyle, row, headers, 0);
			progress = (int) (index * 100.0 / values.size());
			index++;
			// out.flush();
			// System.out.println("progress:"+progress+" %");
		}
		workbook.write(out);
		if (rownum >= MAX_ROWS) {
			System.out.println("MAX_ROWS alcanzado, posible perdida de datos!!!");
			row = sheet.createRow(++rownum);
			Cell cell = row.createCell(0);
			cell.setCellValue("Limite de filas alcanzado, posible perdida de datos de la consulta!!!");
		}
		progress = 100;
		workbook.close();
		// System.out.println("progress:"+progress+" %");
	}

	/**
	 * dado un acncho en pixeles calcula el ancho de la columna
	 * 
	 * @param width
	 * @return
	 */
	private int calculateColWidth(int width) {
		if (width > MAX_ROW_WIDTH_PX) {
			return 65280; // Maximum allowed column width.
		}
		int value = (width * 65280) / MAX_ROW_WIDTH_PX;
		return value;
	}

	private void createData(Object obj, Sheet sheet, CellStyle cellStyle, Row row, List<TableHeader> headers,
			int cellnum) throws Exception {
		Cell cell = null;
		DataFormat df = null;
		Object fieldValue;
		Field field;

		int fr = rownum;
		int fc = cellnum;
		Class<?> cls = obj.getClass();
		for (TableHeader h : headers) {
			field = UtilReflection.getField(h.getFieldName(), cls);
			fieldValue = UtilReflection.getFieldValue(field, obj);
			CellStyle cs = null;


			switch (h.getColumnStyle()) {
				case NONE:
					break;
				case NUMERIC:
					cs = h.getCellStyle();
					if(cs == null) {
						cs = workbook.createCellStyle();
						h.setCellStyle(cs);
						df = workbook.createDataFormat();
						cs.setDataFormat(df.getFormat("#,##0.0"));
					}
					break;
				default:
					cs = cellStyle;
					break;
			}

			if (h.getRender().equals("")) {
				if (h.getSubHeaders().size() > 0) {
					if (fieldValue == null) {
						//System.out.println(String.format("[XSLGenerator] WARNING %s with subHeaders is null", h.getFieldName()));
						continue;
					}
					Iterator<?> iterator = ((Iterable<?>) fieldValue).iterator();
					if (iterator.hasNext()) {
						while (iterator.hasNext()) {
							createData(iterator.next(), sheet, cellStyle, row, h.getSubHeaders(), cellnum);
							if (iterator.hasNext()) {
								if (rownum + 1 > MAX_ROWS)
									break;
								row = sheet.createRow(++rownum);
							}
						}
						if (fr < rownum) {
							for (int i = fc; i < cellnum; i++) {
								sheet.addMergedRegion(new CellRangeAddress(fr, // first row (0-based)
										rownum, // last row (0-based)
										i, // first column (0-based)
										i // last column (0-based)
								));
							}
						}
					}
				} else {
					cell = row.createCell(cellnum++);
					if (fieldValue != null) {
						if (UtilReflection.isNumericType(fieldValue.getClass())) {
							try {
								cell.setCellValue(Double.valueOf(String.valueOf(fieldValue)));
							} catch (NumberFormatException e) {
								cell.setCellValue(String.valueOf(fieldValue));
							}
						} else
							cell.setCellValue(String.valueOf(fieldValue));

					} else
						cell.setCellValue("");
				}
			} else {
				// System.out.println(String.format("obj:%s h:%s fieldValue: %s ",obj,h,
				// fieldValue));
				cell = row.createCell(cellnum++);
				Method render = obj.getClass().getMethod(h.getRender(), field.getType());
				cell.setCellValue(String.valueOf(render.invoke(obj, fieldValue)));
			}
			if(cell != null) {
				cell.setCellStyle(cs);
			}
			if (rownum + 1 > MAX_ROWS)
				return;
		}

	}

	private int createHeaders(Sheet sheet,Row row, List<TableHeader> headers, int cellnum) {
		Cell cell;
		for (TableHeader h : headers) {
			if (h.getSubHeaders().size() > 0 && h.getRender().equals("")) {
				cellnum = createHeaders(sheet,row, h.getSubHeaders(), cellnum);
			} else {
				cell = row.createCell(cellnum);
				cell.setCellValue(h.getHeaderName());
				if (h.getColumnWidth() > 0) {
					sheet.setColumnWidth(cellnum, calculateColWidth(h.getColumnWidth()));
				} else {
					sheet.autoSizeColumn(cellnum, true);
				}
				cellnum++;
			}
		}
		return cellnum;
	}

	private int createDescriptor(Sheet sheet, String reportName, HashMap<String, String> reportParameters) {
		int cellnum = 0;
		Row row = sheet.createRow(rownum);
		Cell cell = row.createCell(cellnum);
		String parametersText = "";

		// Title
		if (reportName != null) {
			cell = row.createCell(cellnum);
			cell.setCellValue(reportName);
		}

		// Date
		row = sheet.createRow(++rownum);
		cell = row.createCell(cellnum);
		DateTime hoy = DateTime.getNow(DateFormat.E_DD_MONTH_NAME_YYYY_HH_mm_ss_time_zone);
		cell.setCellValue("Fecha");
		cell = row.createCell(++cellnum);
		cell.setCellValue(hoy.toString());

		cellnum = 0;
		// Parameters
		if (reportParameters != null && reportParameters.size() > 0) {
			row = sheet.createRow(++rownum);
			cell = row.createCell(cellnum);
			cell.setCellValue("Parámetros");
			cell = row.createCell(++cellnum);

			for (Map.Entry<String, String> param : reportParameters.entrySet()) {
				parametersText += param.getKey() + ": " + param.getValue() + "/ ";
			}

			cell.setCellValue(parametersText.substring(0, parametersText.length() - 2));
		}
		row = sheet.createRow(++rownum);
		return rownum;
	}

	public String getReportName() {
		return reportName;
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
	}

	public HashMap<String, String> getReportParameters() {
		return reportParameters;
	}

	public void setReportParameters(HashMap<String, String> reportParameters) {
		this.reportParameters = reportParameters;
	}

	public boolean isShowDescriptor() {
		return showDescriptor;
	}

	public void setShowDescriptor(boolean showDescriptor) {
		this.showDescriptor = showDescriptor;
	}

	/*
	 * public static void main(String[] args) { HSSFWorkbook wb = Excel.getExcel();
	 * HSSFSheet sheet = wb.createSheet("new sheet"); for(int i=1;i<10;i++){ int fr
	 * = i; Row row = sheet.createRow(i); Cell cell = row.createCell(1);
	 * cell.setCellValue("val i"+i);
	 * 
	 * CellStyle cellStyle = wb.createCellStyle(); //
	 * cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
	 * cellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
	 * cell.setCellStyle(cellStyle);
	 * 
	 * for(int j=1;j<4;j++){ cell = row.createCell(2);
	 * cell.setCellValue("val j:"+j); if(j+1<4) row = sheet.createRow(++i); }
	 * sheet.addMergedRegion(new CellRangeAddress( fr, //first row (0-based) i,
	 * //last row (0-based) 1, //first column (0-based) 1 //last column (0-based)
	 * )); }
	 * 
	 * 
	 * 
	 * try {
	 * 
	 * // Write the output to a file FileOutputStream fileOut = new
	 * FileOutputStream("workbook.xls");
	 * 
	 * wb.write(fileOut); fileOut.close(); } catch (IOException e) {
	 * e.printStackTrace(); } }
	 */
	public static void main(String[] args) {
		System.out.println(Double.valueOf("1,0"));
	}

	public int getProgress() {
		return progress;
	}

}
