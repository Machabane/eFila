/*
 * iDART: The Intelligent Dispensing of Antiretroviral Treatment
 * Copyright (C) 2006 Cell-Life
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License version
 * 2 for more details.
 *
 * You should have received a copy of the GNU General Public License version 2
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.celllife.idart.gui.reportParameters;


import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.celllife.idart.commonobjects.LocalObjects;
import org.celllife.idart.database.dao.ConexaoJDBC;
import org.celllife.idart.gui.platform.GenericReportGui;
import org.celllife.idart.gui.utils.ResourceUtils;
import org.celllife.idart.gui.utils.iDartFont;
import org.celllife.idart.gui.utils.iDartImage;
import org.celllife.idart.misc.iDARTUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.vafada.swtcalendar.SWTCalendar;
import org.vafada.swtcalendar.SWTCalendarEvent;
import org.vafada.swtcalendar.SWTCalendarListener;

import model.manager.reports.HistoricoLevantamentoXLS;
import model.manager.reports.SecondLine;
import model.manager.reports.SecondLinePatients;

/**
 */
public class SecondLineReport extends GenericReportGui {
	
	private Group grpDateRange;

	private SWTCalendar calendarStart;

	private SWTCalendar calendarEnd;
	
	private List<SecondLinePatients> secondLinePatients;
	
    private FileOutputStream out = null;
    
	private final Shell parent;

	/**
	 * Constructor
	 *
	 * @param parent
	 *            Shell
	 * @param activate
	 *            boolean
	 */
	public SecondLineReport(Shell parent, boolean activate) {
		super(parent, REPORTTYPE_MIA, activate);
		this.parent = parent;
	}

	/**
	 * This method initializes newMonthlyStockOverview
	 */
	@Override
	protected void createShell() {
		Rectangle bounds = new Rectangle(100, 50, 600, 510);
		buildShell(REPORT_SECOND_LINE, bounds);
		// create the composites
		createMyGroups();
	}

	private void createMyGroups() {
		createGrpDateInfo();
	}

	/**
	 * This method initializes compHeader
	 *
	 */
	@Override
	protected void createCompHeader() {
		iDartImage icoImage = iDartImage.REPORT_STOCKCONTROLPERCLINIC;
		//buildCompdHeader(REPORT_IDART, icoImage);
		buildCompdHeader("Pacientes em Segunda Linha", icoImage);
	}


	/**
	 * This method initializes grpDateInfo
	 *
	 */
	private void createGrpDateInfo() {
		createGrpDateRange();
	}

	/**
	 * This method initializes compButtons
	 *
	 */
	@Override
	protected void createCompButtons() {
	}

	@SuppressWarnings("unused")
	@Override
	protected void cmdViewReportWidgetSelected() {

		if (iDARTUtil.before(calendarEnd.getCalendar().getTime(), calendarStart.getCalendar().getTime())) {
			showMessage(MessageDialog.ERROR, "End date before start date","You have selected an end date that is before the start date.\nPlease select an end date after the start date.");
			return;
		} else {
			
			try {
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd");

				Date theStartDate = calendarStart.getCalendar().getTime(); 
			
				Date theEndDate=  calendarEnd.getCalendar().getTime();

				Calendar c = Calendar.getInstance(Locale.US);
				c.setLenient(true);
				c.setTime(theStartDate);

				if(Calendar.MONDAY == c.get(Calendar.DAY_OF_WEEK)){
					c.add(Calendar.DAY_OF_WEEK, -2);
					theStartDate = c.getTime();
				}

				SecondLine report = new SecondLine(getShell(), theStartDate, theEndDate);
				viewReport(report);
			} catch (Exception e) {
				getLog().error("Exception while running Second Line report",e);
			}
		}

	}

	@Override
	protected void cmdViewReportXlsWidgetSelected() {
		
		if (iDARTUtil.before(calendarEnd.getCalendar().getTime(), calendarStart.getCalendar().getTime())){
			showMessage(MessageDialog.ERROR, "End date before start date","You have selected an end date that is before the start date.\nPlease select an end date after the start date.");
			return;
		}
					
		try {

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd");
			
			SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");

			 
			Date theStartDate = calendarStart.getCalendar().getTime(); 
		
			Date theEndDate=  calendarEnd.getCalendar().getTime(); 
			
			//theStartDate = sdf.parse(strTheDate);
			
			secondLinePatients = new ArrayList<SecondLinePatients>();

			try {
				ConexaoJDBC con=new ConexaoJDBC();

				secondLinePatients = con.getSecondLinePatients(sdf.format(theStartDate), sdf.format(theEndDate));

				if(secondLinePatients.size() > 0) {

					FileInputStream currentXls = new FileInputStream("Reports/SegundaLinha.xls");

					HSSFWorkbook workbook = new HSSFWorkbook(currentXls);

					HSSFSheet sheet = workbook.getSheetAt(0);

					HSSFCellStyle cellStyle = workbook.createCellStyle();
					cellStyle.setBorderBottom(BorderStyle.THIN);
					cellStyle.setBorderTop(BorderStyle.THIN);
					cellStyle.setBorderLeft(BorderStyle.THIN);
					cellStyle.setBorderRight(BorderStyle.THIN);
					cellStyle.setAlignment(HorizontalAlignment.CENTER);


					HSSFRow healthFacility = sheet.getRow(10);
					HSSFCell healthFacilityCell = healthFacility.createCell(2);
					healthFacilityCell.setCellValue(LocalObjects.currentClinic.getClinicName());
					healthFacilityCell.setCellStyle(cellStyle);

					HSSFRow reportPeriod = sheet.getRow(10);
					HSSFCell reportPeriodCell = reportPeriod.createCell(6);
					reportPeriodCell.setCellValue(sdf.format(theStartDate) +" à "+ sdf.format(theEndDate));
					reportPeriodCell.setCellStyle(cellStyle);

					HSSFRow reportYear = sheet.getRow(11);
					HSSFCell reportYearCell = reportYear.createCell(6);
					reportYearCell.setCellValue(sdfYear.format(theStartDate));
					reportYearCell.setCellStyle(cellStyle);

					  for(int i=14; i<= sheet.getLastRowNum(); i++)
					  {
						Row row = sheet.getRow(i);
					  	deleteRow(sheet,row);
					  }

					  out = new FileOutputStream(new File("Reports/SegundaLinha.xls"));
					  workbook.write(out);

					int rowNum = 14;

					for (SecondLinePatients xls : secondLinePatients) {

						HSSFRow row = sheet.createRow(rowNum++);

						HSSFCell createCellNid = row.createCell(1);
						createCellNid.setCellValue(xls.getPatientIdentifier());
						createCellNid.setCellStyle(cellStyle);


						HSSFCell createCellNome = row.createCell(2);
						createCellNome.setCellValue(xls.getNome());
						createCellNome.setCellStyle(cellStyle);

						HSSFCell createCellAge = row.createCell(3);
						createCellAge.setCellValue(xls.getIdade());
						createCellAge.setCellStyle(cellStyle);

						HSSFCell createCellTherapeuticScheme = row.createCell(4);
						createCellTherapeuticScheme.setCellValue(xls.getTherapeuticScheme());
						createCellTherapeuticScheme.setCellStyle(cellStyle);

						HSSFCell createCellLine = row.createCell(5);
						createCellLine.setCellValue(xls.getLine());
						createCellLine.setCellStyle(cellStyle);

						HSSFCell createCellArtType = row.createCell(6);
						createCellArtType.setCellValue(xls.getArtType());
						createCellArtType.setCellStyle(cellStyle);
					}

					for(int i = 1; i < SecondLine.class.getClass().getDeclaredFields().length; i++) {
			            sheet.autoSizeColumn(i);
			        }

					currentXls.close();

					FileOutputStream outputStream = new FileOutputStream(new File("TemplateHistoricoLevantamento.xls"));
					workbook.write(outputStream);
					workbook.close();

					Desktop.getDesktop().open(new File("TemplateHistoricoLevantamento.xls"));

				} else {
					MessageBox mNoPages = new MessageBox(parent,SWT.ICON_ERROR | SWT.OK);
					mNoPages.setText("O relatório não possui páginas");
					mNoPages.setMessage("O relatório que estás a gerar não contém nenhum dado. \n\nVerifique os valores de entrada que inseriu (como datas) para este relatório e tente novamente.");
					mNoPages.open();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			getLog().error("Exception while running Historico levantamento report",e);
		}
	}
	
	private void deleteRow(HSSFSheet sheet, Row row) {
		int lastRowNum = sheet.getLastRowNum();
		if (lastRowNum > 0) {
			int rowIndex = row.getRowNum();
			Row removingRow = sheet.getRow(rowIndex);
			if (removingRow != null) {
				sheet.removeRow(removingRow);
				System.out.println("Deleting.... ");
			}
		}
	}

	/**
	 * This method is called when the user presses "Close" button
	 *
	 */
	@Override
	protected void cmdCloseWidgetSelected() {
		cmdCloseSelected();
	}

	@Override
	protected void setLogger() {
		setLog(Logger.getLogger(this.getClass()));
	}

	
	private void createGrpDateRange() {
		grpDateRange = new Group(getShell(), SWT.NONE);
		grpDateRange.setText("Período:");
		grpDateRange.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		grpDateRange.setBounds(new Rectangle(55, 160, 520, 201));
		grpDateRange.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

		Label lblStartDate = new Label(grpDateRange, SWT.CENTER | SWT.BORDER);
		lblStartDate.setBounds(new org.eclipse.swt.graphics.Rectangle(40, 30,
				180, 20));
		lblStartDate.setText("Data Início:");
		lblStartDate.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

		Label lblEndDate = new Label(grpDateRange, SWT.CENTER | SWT.BORDER);
		lblEndDate.setBounds(new org.eclipse.swt.graphics.Rectangle(300, 30,
				180, 20));
		lblEndDate.setText("Data Fim:");
		lblEndDate.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

		calendarStart = new SWTCalendar(grpDateRange);
		calendarStart.setBounds(20, 55, 220, 140);

		calendarEnd = new SWTCalendar(grpDateRange);
		calendarEnd.setBounds(280, 55, 220, 140);
		calendarEnd.addSWTCalendarListener(new SWTCalendarListener() {
			@Override
			public void dateChanged(SWTCalendarEvent calendarEvent) {
				Date date = calendarEvent.getCalendar().getTime();
			}
		});
	}
	
	/**
	 * Method getCalendarEnd.
	 * @return Calendar
	 */
	public Calendar getCalendarEnd() {
		return calendarEnd.getCalendar();
	}
	
	/**
	 * Method setEndDate.
	 * @param date Date
	 */
	public void setEndtDate(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendarEnd.setCalendar(calendar);
	}
	
	/**
	 * Method addEndDateChangedListener.
	 * @param listener SWTCalendarListener
	 */
	public void addEndDateChangedListener(SWTCalendarListener listener) {
		calendarEnd.addSWTCalendarListener(listener);
	}
	
	/**
	 * Method getCalendarStart.
	 * @return Calendar
	 */
	public Calendar getCalendarStart() {
		return calendarStart.getCalendar();
	}
	
	/**
	 * Method setStartDate.
	 * @param date Date
	 */
	public void setStartDate(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendarStart.setCalendar(calendar);
	}
	
	/**
	 * Method addStartDateChangedListener.
	 * @param listener SWTCalendarListener
	 */
	public void addStartDateChangedListener(SWTCalendarListener listener) {
		calendarStart.addSWTCalendarListener(listener);
	}
}
