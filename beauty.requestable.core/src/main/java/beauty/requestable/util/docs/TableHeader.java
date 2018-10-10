package beauty.requestable.util.docs;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.CellStyle;

import beauty.requestable.util.reports.annotations.ColumnFormat;


public class TableHeader {
		private String fieldName;
		private String headerName;
		private String type ="text";
		private String render="";
		private int columnWidth = 0;
		private ColumnFormat columnStyle = ColumnFormat.NONE;
		
		private CellStyle cellStyle = null;
		
		private List<TableHeader> subHeaders = new ArrayList<TableHeader>();
		
		public TableHeader(String fieldName, String headerName) {
			this.setFieldName(fieldName);
			this.setHeaderName(headerName);
		}
		public TableHeader(String fieldName, String headerName,String type) {
			this.setFieldName(fieldName);
			this.setHeaderName(headerName);
			this.type = type;
		}
		public String getFieldName() {
			return fieldName;
		}
		public void setFieldName(String fieldName) {
			this.fieldName = fieldName;
		}
		public String getHeaderName() {
			return headerName;
		}
		public void setHeaderName(String headerName) {
			if(headerName != null && !headerName.equals(""))
				this.headerName = headerName;
			else
				this.headerName = this.fieldName;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public List<TableHeader> getSubHeaders() {
			return subHeaders;
		}
		public void setSubHeaders(List<TableHeader> subHeaders) {
			this.subHeaders = subHeaders;
		}
		public String getRender() {
			return render;
		}
		public void setRender(String render) {
			this.render = render;
		}
		public int getColumnWidth() {
			return columnWidth;
		}
		public void setColumnWidth(int columnWidth) {
			this.columnWidth = columnWidth;
		}
		public ColumnFormat getColumnStyle() {
			return columnStyle;
		}
		public void setColumnStyle(ColumnFormat columnStyle) {
			this.columnStyle = columnStyle;
		}
		public CellStyle getCellStyle() {
			return cellStyle;
		}
		public void setCellStyle(CellStyle cellStyle) {
			this.cellStyle = cellStyle;
		}
		@Override
		public String toString() {
			return String.format(
					"TableHeader [fieldName=%s, headerName=%s, type=%s, render=%s, columnWidth=%s, columnStyle=%s, cellStyle=%s, subHeaders=%s]",
					fieldName, headerName, type, render, columnWidth, columnStyle, cellStyle, subHeaders);
		}
		
	}
