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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.celllife.idart.commonobjects.CommonObjects;
import org.celllife.idart.commonobjects.LocalObjects;
import org.celllife.idart.database.dao.ConexaoJDBC;
import org.celllife.idart.gui.platform.GenericReportGui;
import org.celllife.idart.gui.platform.GenericReportGuiInterface;
import org.celllife.idart.gui.utils.ResourceUtils;
import org.celllife.idart.gui.utils.iDartColor;
import org.celllife.idart.gui.utils.iDartFont;
import org.celllife.idart.gui.utils.iDartImage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.vafada.swtcalendar.SWTCalendar;
import org.vafada.swtcalendar.SWTCalendarListener;

import model.manager.reports.FollowupFaulty;
import model.manager.reports.MissedAppointmentsReportDS;
import model.manager.reports.MissedAppointmentsReportDT;

/**
 */
public class MissedAppointmentsDS extends GenericReportGui {

	private Group grpClinicSelection;

	private Label lblClinic;

	private CCombo cmbClinic;

	private Label lblMinimumDaysLate;

	private Text txtMinimumDaysLate;

	private Text txtMaximumDaysLate;

	private Label lblMaximumDaysLate;

	private Group grpDateRange;

	private SWTCalendar swtCal;
	
	private final Shell parent;
	
    private List<FollowupFaulty> lostToFollowupFaultySemiAnnual;
    
    private FileOutputStream out = null; 

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            Shell
	 * @param activate
	 *            boolean
	 */
	public MissedAppointmentsDS(Shell parent, boolean activate) {
		super(parent, GenericReportGuiInterface.REPORTTYPE_CLINICMANAGEMENT,
				activate);
		this.parent = parent;
	}

	/**
	 * This method initializes newMonthlyStockOverview
	 */
	@Override
	protected void createShell() {
		buildShell(REPORT_MISSED_APPOINTMENTS_DS, new Rectangle(100, 50, 600,
				510));
		// create the composites
		createMyGroups();
	}

	private void createMyGroups() {
		createGrpClinicSelection();
		createGrpDateRange();
	}

	/**
	 * This method initializes compHeader
	 * 
	 */
	@Override
	protected void createCompHeader() {
		iDartImage icoImage = iDartImage.REPORT_PATIENTDEFAULTERS;
		buildCompdHeader(REPORT_MISSED_APPOINTMENTS_DS, icoImage);
	}

	/**
	 * This method initializes grpClinicSelection
	 * 
	 */
	private void createGrpClinicSelection() {

		grpClinicSelection = new Group(getShell(), SWT.NONE);
		grpClinicSelection.setText("Configuração do Relatório de Faltosos e/ou Abandonos na Dispensa Semenstral");
		grpClinicSelection.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		grpClinicSelection.setBounds(new Rectangle(60, 79, 465, 114));

		lblClinic = new Label(grpClinicSelection, SWT.NONE);
		lblClinic.setBounds(new Rectangle(30, 25, 167, 20));
		lblClinic.setText("US");
		lblClinic.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

		cmbClinic = new CCombo(grpClinicSelection, SWT.BORDER);
		cmbClinic.setBounds(new Rectangle(202, 25, 160, 20));
		cmbClinic.setEditable(false);
		cmbClinic.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		cmbClinic.setBackground(ResourceUtils.getColor(iDartColor.WHITE));

		CommonObjects.populateClinics(getHSession(), cmbClinic);

		lblMinimumDaysLate = new Label(grpClinicSelection, SWT.NONE);
		lblMinimumDaysLate.setBounds(new Rectangle(31, 57, 147, 21));
		lblMinimumDaysLate.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		lblMinimumDaysLate.setText("Dias Mínimos de Atraso:");

		txtMinimumDaysLate = new Text(grpClinicSelection, SWT.BORDER);
		txtMinimumDaysLate.setBounds(new Rectangle(201, 56, 45, 20));
		txtMinimumDaysLate.setText("0");
              //  txtMinimumDaysLate.setEditable(false);
		txtMinimumDaysLate.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

		lblMaximumDaysLate = new Label(grpClinicSelection, SWT.NONE);
		lblMaximumDaysLate.setBounds(new Rectangle(31, 86, 150, 21));
		lblMaximumDaysLate.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		lblMaximumDaysLate.setText("Dias Máxímos de Atraso:");

		txtMaximumDaysLate = new Text(grpClinicSelection, SWT.BORDER);
		txtMaximumDaysLate.setBounds(new Rectangle(202, 86, 43, 19));
		txtMaximumDaysLate.setText("90");
               // txtMaximumDaysLate.setEditable(false);
		txtMaximumDaysLate.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

	}

	/**
	 * This method initializes grpDateRange
	 * 
	 */
	private void createGrpDateRange() {

		grpDateRange = new Group(getShell(), SWT.NONE);
		grpDateRange.setText("Seleccione a data de reporte:");
		grpDateRange.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		grpDateRange.setBounds(new Rectangle(142, 214, 309, 211));

		swtCal = new SWTCalendar(grpDateRange);
		swtCal.setBounds(40, 40, 220, 160);

	}

	/**
	 * Method getCalendarDate.
	 * 
	 * @return Calendar
	 */
	public Calendar getCalendarDate() {
		return swtCal.getCalendar();
	}

	/**
	 * Method setCalendarDate.
	 * 
	 * @param date
	 *            Date
	 */
	public void setCalendarDate(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		swtCal.setCalendar(calendar);
	}

	/**
	 * Method addDateChangedListener.
	 * 
	 * @param listener
	 *            SWTCalendarListener
	 */
	public void addDateChangedListener(SWTCalendarListener listener) {

		swtCal.addSWTCalendarListener(listener);
	}

	/**
	 * This method initializes compButtons
	 * 
	 */
	@Override
	protected void createCompButtons() {
	}

	@Override
	protected void cmdViewReportWidgetSelected() {

		boolean viewReport = true;
		int max = 0;
		int min = 0;

		if (cmbClinic.getText().equals("")) {

			MessageBox missing = new MessageBox(getShell(), SWT.ICON_ERROR
					| SWT.OK);
			missing.setText("No Clinic Was Selected");
			missing
			.setMessage("No clinic was selected. Please select a clinic by looking through the list of available clinics.");
			missing.open();
			viewReport = false;

		}

		if (txtMinimumDaysLate.getText().equals("")
				|| txtMaximumDaysLate.getText().equals("")) {
			MessageBox incorrectData = new MessageBox(getShell(),
					SWT.ICON_ERROR | SWT.OK);
			incorrectData.setText("Invalid Information");
			incorrectData
			.setMessage("The minimum and maximum days late must both be numbers.");
			incorrectData.open();
			txtMinimumDaysLate.setText("");
			txtMinimumDaysLate.setFocus();
			viewReport = false;
		} else if (!txtMinimumDaysLate.getText().equals("")
				&& !txtMaximumDaysLate.getText().equals("")) {
			try {
				min = Integer.parseInt(txtMinimumDaysLate.getText());
				max = Integer.parseInt(txtMaximumDaysLate.getText());

				if ((min < 0) || (max < 0)) {
					MessageBox incorrectData = new MessageBox(getShell(),
							SWT.ICON_ERROR | SWT.OK);
					incorrectData.setText("Invalid Information");
					incorrectData
					.setMessage("The minimum and maximum days late must both be positive numbers.");
					incorrectData.open();
					txtMinimumDaysLate.setText("");
					txtMinimumDaysLate.setFocus();
					viewReport = false;
				}

				if (min >= max) {
					MessageBox incorrectData = new MessageBox(getShell(),
							SWT.ICON_ERROR | SWT.OK);
					incorrectData.setText("Invalid Information");
					incorrectData
					.setMessage("The minimum days late must be smaller than the maximum days late.");
					incorrectData.open();
					txtMinimumDaysLate.setFocus();
					viewReport = false;
				}

			} catch (NumberFormatException nfe) {
				MessageBox incorrectData = new MessageBox(getShell(),
						SWT.ICON_ERROR | SWT.OK);
				incorrectData.setText("Invalid Information");
				incorrectData
				.setMessage("The minimum and maximum days late must both be whole numbers.");
				incorrectData.open();
				txtMinimumDaysLate.setText("");
				txtMinimumDaysLate.setFocus();
				viewReport = false;
			}
		}

		if (viewReport) {
                        MissedAppointmentsReportDS report = new MissedAppointmentsReportDS(getShell(),cmbClinic.getText(),
                        Integer.parseInt(txtMinimumDaysLate.getText()),
                        Integer.parseInt(txtMaximumDaysLate.getText()),
                        swtCal.getCalendar().getTime());
			viewReport(report);
		}

	}

	@Override
	protected void cmdViewReportXlsWidgetSelected() {
		
		ConexaoJDBC con = new ConexaoJDBC();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");
		
		boolean viewReport = true;
		
		int max = 0;
		int min = 0;

		if (cmbClinic.getText().equals("")) {

			MessageBox missing = new MessageBox(getShell(), SWT.ICON_ERROR
					| SWT.OK);
			missing.setText("No Clinic Was Selected");
			missing
			.setMessage("No clinic was selected. Please select a clinic by looking through the list of available clinics.");
			missing.open();
			viewReport = false;

		}

		if (txtMinimumDaysLate.getText().equals("")
				|| txtMaximumDaysLate.getText().equals("")) {
			MessageBox incorrectData = new MessageBox(getShell(),
					SWT.ICON_ERROR | SWT.OK);
			incorrectData.setText("Invalid Information");
			incorrectData
			.setMessage("The minimum and maximum days late must both be numbers.");
			incorrectData.open();
			txtMinimumDaysLate.setText("");
			txtMinimumDaysLate.setFocus();
			viewReport = false;
		} else if (!txtMinimumDaysLate.getText().equals("")
				&& !txtMaximumDaysLate.getText().equals("")) {
			try {
				min = Integer.parseInt(txtMinimumDaysLate.getText());
				max = Integer.parseInt(txtMaximumDaysLate.getText());

				if ((min < 0) || (max < 0)) {
					MessageBox incorrectData = new MessageBox(getShell(),
							SWT.ICON_ERROR | SWT.OK);
					incorrectData.setText("Invalid Information");
					incorrectData
					.setMessage("The minimum and maximum days late must both be positive numbers.");
					incorrectData.open();
					txtMinimumDaysLate.setText("");
					txtMinimumDaysLate.setFocus();
					viewReport = false;
				}

				if (min >= max) {
					MessageBox incorrectData = new MessageBox(getShell(),
							SWT.ICON_ERROR | SWT.OK);
					incorrectData.setText("Invalid Information");
					incorrectData
					.setMessage("The minimum days late must be smaller than the maximum days late.");
					incorrectData.open();
					txtMinimumDaysLate.setFocus();
					viewReport = false;
				}

			} catch (NumberFormatException nfe) {
				MessageBox incorrectData = new MessageBox(getShell(),
						SWT.ICON_ERROR | SWT.OK);
				incorrectData.setText("Invalid Information");
				incorrectData
				.setMessage("The minimum and maximum days late must both be whole numbers.");
				incorrectData.open();
				txtMinimumDaysLate.setText("");
				txtMinimumDaysLate.setFocus();
				viewReport = false;
			}
		}
		
		if (viewReport) {
			
			lostToFollowupFaultySemiAnnual = new ArrayList<FollowupFaulty>();
			
			try {
				lostToFollowupFaultySemiAnnual = con.lostToFollowupFaultySemiAnnual(txtMinimumDaysLate.getText(), txtMaximumDaysLate.getText(), 
						sdf.format(swtCal.getCalendar().getTime()), String.valueOf(LocalObjects.mainClinic.getId()));
				
				if(lostToFollowupFaultySemiAnnual.size() > 0) {
					
					FileInputStream currentXls = new FileInputStream("Reports/FaltososAbandonosDS.xls");
					
					HSSFWorkbook workbook = new HSSFWorkbook(currentXls);
					
					HSSFSheet sheet = workbook.getSheetAt(0);
					
					HSSFCellStyle cellStyle = workbook.createCellStyle();
					cellStyle.setBorderBottom(BorderStyle.THIN);
					cellStyle.setBorderTop(BorderStyle.THIN);
					cellStyle.setBorderLeft(BorderStyle.THIN);
					cellStyle.setBorderRight(BorderStyle.THIN);
					cellStyle.setAlignment(HorizontalAlignment.CENTER);
					
					HSSFCellStyle cellFontStyle = workbook.createCellStyle();
					HSSFFont font = workbook.createFont();
					font.setFontHeightInPoints((short) 14); 
					cellFontStyle.setFont(font );
											
					HSSFRow healthFacility = sheet.getRow(10); 
					HSSFCell healthFacilityCell = healthFacility.createCell(2); 
					healthFacilityCell.setCellValue(LocalObjects.currentClinic.getClinicName());
					healthFacilityCell.setCellStyle(cellStyle); 
					
					HSSFRow reportPeriod = sheet.getRow(10);
					HSSFCell reportPeriodCell = reportPeriod.createCell(7);
					reportPeriodCell.setCellValue(sdf.format(swtCal.getCalendar().getTime()));
					reportPeriodCell.setCellStyle(cellStyle); 

					HSSFRow reportYear = sheet.getRow(11);
					HSSFCell reportYearCell = reportYear.createCell(7);
					reportYearCell.setCellValue(sdfYear.format(swtCal.getCalendar().getTime()));
					reportYearCell.setCellStyle(cellStyle);
					
					HSSFRow minMax = sheet.getRow(8);
					HSSFCell minMaxCell = minMax.createCell(4);
					minMaxCell.setCellValue("Este relatório mostra os pacientes \nque faltaram entre " + txtMinimumDaysLate.getText() + " e " + txtMaximumDaysLate.getText() + " dias");
					minMaxCell.setCellStyle(cellFontStyle); 

					  for(int i=14; i<= sheet.getLastRowNum(); i++) 
					  { 
						HSSFRow row = sheet.getRow(i);
					  	deleteRow(sheet,row);  
					  }
					 
					  out = new FileOutputStream(new File("Reports/FaltososAbandonosDS.xls"));
					  workbook.write(out); 
					
					int rowNum = 14;
					
					for (FollowupFaulty xls : lostToFollowupFaultySemiAnnual) { 
						
						HSSFRow row = sheet.createRow(rowNum++);
						
						HSSFCell createCellNid = row.createCell(1);
						createCellNid.setCellValue(xls.getPatientIdentifier());
						createCellNid.setCellStyle(cellStyle); 
						
						HSSFCell createCellNome = row.createCell(2);
						createCellNome.setCellValue(xls.getNome());
						createCellNome.setCellStyle(cellStyle);

						HSSFCell createCellDataQueFaltouLevantamento = row.createCell(3);
						createCellDataQueFaltouLevantamento.setCellValue(xls.getDataQueFaltouLevantamento());
						createCellDataQueFaltouLevantamento.setCellStyle(cellStyle);

						HSSFCell createCellDataIdentificouAbandonoTarv = row.createCell(4); 
						createCellDataIdentificouAbandonoTarv.setCellValue(xls.getDataIdentificouAbandonoTarv());
						createCellDataIdentificouAbandonoTarv.setCellStyle(cellStyle);
						
						HSSFCell emptyCell_1 = row.createCell(5); 
						emptyCell_1.setCellValue("");
						emptyCell_1.setCellStyle(cellStyle);
						
						HSSFCell createCellEfectuouLigacao = row.createCell(6); 
						createCellEfectuouLigacao.setCellValue(xls.getDataRegressouUnidadeSanitaria());
						createCellEfectuouLigacao.setCellStyle(cellStyle);
						
						HSSFCell emptyCell_2 = row.createCell(7); 
						emptyCell_2.setCellValue("");
						emptyCell_2.setCellStyle(cellStyle);
					}
					
					currentXls.close();
					
					FileOutputStream outputStream = new FileOutputStream(new File("Reports/FaltososAbandonosDS.xls")); 
					workbook.write(outputStream);
					workbook.close();
					
					Desktop.getDesktop().open(new File("Reports/FaltososAbandonosDS.xls"));
					
				} else {
					MessageBox mNoPages = new MessageBox(parent,SWT.ICON_ERROR | SWT.OK);
					mNoPages.setText("O relatório não possui páginas");
					mNoPages.setMessage("O relatório que estás a gerar não contém nenhum dado. \\ n \\ n Verifique os valores de entrada que inseriu (como datas) para este relatório e tente novamente.");
					mNoPages.open();
				}
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void deleteRow(HSSFSheet sheet, Row row) {
		int lastRowNum = sheet.getLastRowNum();
		if (lastRowNum > 0) {
			int rowIndex = row.getRowNum();
			HSSFRow removingRow = sheet.getRow(rowIndex);
			if (removingRow != null) {
				sheet.removeRow(removingRow);
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
}
