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
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.celllife.idart.commonobjects.CommonObjects;
import org.celllife.idart.commonobjects.LocalObjects;
import org.celllife.idart.database.dao.ConexaoJDBC;
import org.celllife.idart.database.hibernate.StockCenter;
import org.celllife.idart.gui.platform.GenericReportGui;
import org.celllife.idart.gui.utils.ResourceUtils;
import org.celllife.idart.gui.utils.iDartColor;
import org.celllife.idart.gui.utils.iDartFont;
import org.celllife.idart.gui.utils.iDartImage;
import org.celllife.idart.misc.iDARTUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.vafada.swtcalendar.SWTCalendar;
import org.vafada.swtcalendar.SWTCalendarEvent;
import org.vafada.swtcalendar.SWTCalendarListener;

import model.manager.AdministrationManager;
import model.manager.reports.DispensaTrimestral;
import model.manager.reports.DispensaTrimestralSemestral;

/**
 */
public class DispensaTrimestralReport extends GenericReportGui {

    private Group grpDateRange;

    private SWTCalendar calendarStart;

    private SWTCalendar calendarEnd;

    private Group grpPharmacySelection;

    private CCombo cmbStockCenter;
    
    private StockCenter pharm;
    
    private final Shell parent;
    
	private List<DispensaTrimestralSemestral> dispensaTrimestralXLS;
	
	private FileOutputStream out = null;

    /**
     * Constructor
     *
     * @param parent Shell
     * @param activate boolean
     */
    public DispensaTrimestralReport(Shell parent, boolean activate) {
        super(parent, REPORTTYPE_MIA, activate);
        this.parent = parent;
    }

    /**
     * This method initializes newMonthlyStockOverview
     */
    @Override
    protected void createShell() {
        Rectangle bounds = new Rectangle(100, 50, 600, 510);
        buildShell(REPORT_DISPENSA_TRIMESTRAL, bounds);
        // create the composites
        createMyGroups();
    }

    private void createMyGroups() {
        createGrpClinicSelection();
        createGrpDateInfo();
    }

    /**
     * This method initializes compHeader
     *
     */
    @Override
    protected void createCompHeader() {
        iDartImage icoImage = iDartImage.REPORT_STOCKCONTROLPERCLINIC;
        buildCompdHeader("Pacientes em dispensa trimestral", icoImage);
    }

    /**
     * This method initializes grpClinicSelection
     *
     */
    private void createGrpClinicSelection() {

        grpPharmacySelection = new Group(getShell(), SWT.NONE);
        grpPharmacySelection.setText("Farmácia");
        grpPharmacySelection.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        grpPharmacySelection.setBounds(new org.eclipse.swt.graphics.Rectangle(140, 90, 320, 65));

        Label lblPharmacy = new Label(grpPharmacySelection, SWT.NONE);
        lblPharmacy.setBounds(new Rectangle(10, 25, 140, 20));
        lblPharmacy.setText("Selecione a farmácia");
        lblPharmacy.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        cmbStockCenter = new CCombo(grpPharmacySelection, SWT.BORDER);
        cmbStockCenter.setBounds(new Rectangle(156, 24, 160, 20));
        cmbStockCenter.setEditable(false);
        cmbStockCenter.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        cmbStockCenter.setBackground(ResourceUtils.getColor(iDartColor.WHITE));

        CommonObjects.populateStockCenters(getHSession(), cmbStockCenter);

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

        pharm = AdministrationManager.getStockCenter(getHSession(),cmbStockCenter.getText());

        if (cmbStockCenter.getText().equals("")) {

            MessageBox missing = new MessageBox(getShell(), SWT.ICON_ERROR
                    | SWT.OK);
            missing.setText("Nenhuma farmácia foi selecionada");
            missing.setMessage("Nenhuma farmácia foi selecionada. Selecione uma farmácia da lista de farmácias disponíveis.");
            missing.open();

        } else if (pharm == null) {

            MessageBox missing = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
            missing.setText("Farmácia não encontrada");
            missing.setMessage("There is no pharmacy called '"
                            + cmbStockCenter.getText()
                            + "' in the database. Please select a pharmacy by looking through the list of available pharmacies.");
            missing.open();

        } else if (iDARTUtil.before(calendarEnd.getCalendar().getTime(), calendarStart.getCalendar().getTime())) {
            showMessage(MessageDialog.ERROR, "End date before start date","You have selected an end date that is before the start date.\nPlease select an end date after the start date.");
            return;
        } else {
            try {

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd");

                Date theStartDate = calendarStart.getCalendar().getTime();

                Date theEndDate = calendarEnd.getCalendar().getTime();

                Calendar c = Calendar.getInstance(Locale.US);
                c.setLenient(true);
                c.setTime(theStartDate);

                if(Calendar.MONDAY == c.get(Calendar.DAY_OF_WEEK)){
                    c.add(Calendar.DAY_OF_WEEK, -2);
                    theStartDate = c.getTime();
                }

                DispensaTrimestral report = new DispensaTrimestral(getShell(), theStartDate, theEndDate);
                viewReport(report);
            } catch (Exception e) {
                getLog().error("Exception while running Monthly Receipts and Issues report",e);
           
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
     *
     * @return Calendar
     */
    public Calendar getCalendarEnd() {
        return calendarEnd.getCalendar();
    }

    /**
     * Method setEndDate.
     *
     * @param date Date
     */
    public void setEndtDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendarEnd.setCalendar(calendar);
    }

    /**
     * Method addEndDateChangedListener.
     *
     * @param listener SWTCalendarListener
     */
    public void addEndDateChangedListener(SWTCalendarListener listener) {

        calendarEnd.addSWTCalendarListener(listener);
    }

    /**
     * Method getCalendarStart.
     *
     * @return Calendar
     */
    public Calendar getCalendarStart() {
        return calendarStart.getCalendar();
    }

    /**
     * Method setStartDate.
     *
     * @param date Date
     */
    public void setStartDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendarStart.setCalendar(calendar);
    }

    /**
     * Method addStartDateChangedListener.
     *
     * @param listener SWTCalendarListener
     */
    public void addStartDateChangedListener(SWTCalendarListener listener) {

        calendarStart.addSWTCalendarListener(listener);
    }

	@Override
	protected void cmdViewReportXlsWidgetSelected() {

        pharm = AdministrationManager.getStockCenter(getHSession(),cmbStockCenter.getText());

        if (cmbStockCenter.getText().equals("")) {

            MessageBox missing = new MessageBox(getShell(), SWT.ICON_ERROR
                    | SWT.OK);
            missing.setText("Nenhuma farmácia foi selecionada");
            missing
                    .setMessage("No pharmacy was selected. Please select a pharmacy by looking through the list of available pharmacies.");
            missing.open();

        } else if (pharm == null) {

            MessageBox missing = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
            missing.setText("Pharmacy not found");
            missing.setMessage("There is no pharmacy called '"
                            + cmbStockCenter.getText()
                            + "' in the database. Please select a pharmacy by looking through the list of available pharmacies.");
            missing.open();

        } else if (iDARTUtil.before(calendarEnd.getCalendar().getTime(), calendarStart.getCalendar().getTime())) {
            showMessage(MessageDialog.ERROR, "End date before start date","You have selected an end date that is before the start date.\nPlease select an end date after the start date.");
            return;
        } 
        
        dispensaTrimestralXLS = new ArrayList<DispensaTrimestralSemestral>();
		
		try {
			ConexaoJDBC con = new ConexaoJDBC();
			
			Map<String, Object> map = new HashMap<>();
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			
			 Map mapaDispensaTrimestral = con.DispensaTrimestral(dateFormat.format(calendarStart.getCalendar().getTime()),dateFormat.format(calendarEnd.getCalendar().getTime()));
			
            int totalpacientesManter = Integer.parseInt(mapaDispensaTrimestral.get("totalpacientesmanter").toString());
            int totalpacientesNovos = Integer.parseInt(mapaDispensaTrimestral.get("totalpacientesnovos").toString());
            int totalpacienteManuntencaoTransporte = Integer.parseInt(mapaDispensaTrimestral.get("totalpacienteManuntencaoTransporte").toString());
            int totalpacienteCumulativo = Integer.parseInt(mapaDispensaTrimestral.get("totalpacienteCumulativo").toString());
			
			dispensaTrimestralXLS = con.dispensaTrimestral(dateFormat.format(calendarStart.getCalendar().getTime()), dateFormat.format(calendarEnd.getCalendar().getTime()));
			
			if(dispensaTrimestralXLS.size() > 0) {
				
				FileInputStream currentXls = new FileInputStream("Reports/DispensaTrimestral.xls");
				
				HSSFWorkbook workbook = new HSSFWorkbook(currentXls);
				
				HSSFSheet sheet = workbook.getSheetAt(0);
				
				HSSFCellStyle cellStyle = workbook.createCellStyle();
				cellStyle.setBorderBottom(BorderStyle.THIN);
				cellStyle.setBorderTop(BorderStyle.THIN);
				cellStyle.setBorderLeft(BorderStyle.THIN);
				cellStyle.setBorderRight(BorderStyle.THIN);
				cellStyle.setAlignment(HorizontalAlignment.CENTER);

										
				HSSFRow healthFacility = sheet.getRow(9); 
				HSSFCell healthFacilityCell = healthFacility.createCell(2); 
				healthFacilityCell.setCellValue(LocalObjects.currentClinic.getClinicName());
				healthFacilityCell.setCellStyle(cellStyle); 
				
				HSSFRow reportPeriod = sheet.getRow(10);
				HSSFCell reportPeriodCell = reportPeriod.createCell(2);
				reportPeriodCell.setCellValue(dateFormat.format(calendarStart.getCalendar().getTime()) +" à "+ dateFormat.format(calendarEnd.getCalendar().getTime()));
				reportPeriodCell.setCellStyle(cellStyle);
				
				HSSFRow _totalpacientesNovos = sheet.getRow(14); 
				HSSFCell totalpacientesNovosCell = _totalpacientesNovos.createCell(2); 
				totalpacientesNovosCell.setCellValue(totalpacientesNovos);
				totalpacientesNovosCell.setCellStyle(cellStyle);
				
				HSSFRow _totalpacientesManter = sheet.getRow(15); 
				HSSFCell totalpacientesManterCell = _totalpacientesManter.createCell(2); 
				totalpacientesManterCell.setCellValue(totalpacientesManter);
				totalpacientesManterCell.setCellStyle(cellStyle);
				
				HSSFRow _totalpacienteManuntencaoTransporte = sheet.getRow(16); 
				HSSFCell totalpacienteManuntencaoTransporteCell = _totalpacienteManuntencaoTransporte.createCell(2); 
				totalpacienteManuntencaoTransporteCell.setCellValue(totalpacienteManuntencaoTransporte);
				totalpacienteManuntencaoTransporteCell.setCellStyle(cellStyle); 
				
				HSSFRow _totalpacienteCumulativo = sheet.getRow(17); 
				HSSFCell totalpacienteCumulativoCell = _totalpacienteCumulativo.createCell(2); 
				totalpacienteCumulativoCell.setCellValue(totalpacienteCumulativo);
				totalpacienteCumulativoCell.setCellStyle(cellStyle);

				  for(int i=21; i<= sheet.getLastRowNum(); i++) 
				  { 
					HSSFRow row = sheet.getRow(i);
				  	deleteRow(sheet,row);  
				  }
				 
				  out = new FileOutputStream(new File("Reports/DispensaTrimestral.xls"));
				  workbook.write(out); 
				
				int rowNum = 21;
				
				for (DispensaTrimestralSemestral xls : dispensaTrimestralXLS) { 
					
					HSSFRow row = sheet.createRow(rowNum++);
					
					HSSFCell createCellNid = row.createCell(1);
					createCellNid.setCellValue(xls.getPatientIdentifier());
					createCellNid.setCellStyle(cellStyle); 
					
					HSSFCell createCellNome = row.createCell(2);
					createCellNome.setCellValue(xls.getNome());
					createCellNome.setCellStyle(cellStyle);
					
					HSSFCell createCellRegimeTerapeutico = row.createCell(3); 
					createCellRegimeTerapeutico.setCellValue(xls.getRegimeTerapeutico());
					createCellRegimeTerapeutico.setCellStyle(cellStyle);

					HSSFCell createCellTipoTarv = row.createCell(4);
					createCellTipoTarv.setCellValue(xls.getTipoPaciente());
					createCellTipoTarv.setCellStyle(cellStyle);


					HSSFCell createCellDataPrescricao = row.createCell(5); 
					createCellDataPrescricao.setCellValue(xls.getDataPrescricao());
					createCellDataPrescricao.setCellStyle(cellStyle);

					HSSFCell createCellDataLevantamento = row.createCell(6); 
					createCellDataLevantamento.setCellValue(xls.getDataLevantamento());
					createCellDataLevantamento.setCellStyle(cellStyle);

					HSSFCell createCellDataProximoLevantamento = row.createCell(7);
					createCellDataProximoLevantamento.setCellValue(xls.getDataProximoLevantamento());
					createCellDataProximoLevantamento.setCellStyle(cellStyle);
				}
				
				for(int i = 1; i < DispensaTrimestralSemestral.class.getClass().getDeclaredFields().length; i++) { 
		            sheet.autoSizeColumn(i);
		        }
				
				currentXls.close();
				
				FileOutputStream outputStream = new FileOutputStream(new File("Reports/DispensaTrimestral.xls")); 
				workbook.write(outputStream);
				workbook.close();
				
				Desktop.getDesktop().open(new File("Reports/DispensaTrimestral.xls"));
				
			} else {
				MessageBox mNoPages = new MessageBox(parent,SWT.ICON_ERROR | SWT.OK);
				mNoPages.setText("O relatório não possui páginas");
				mNoPages.setMessage("O relatório que estás a gerar não contém nenhum dado. \\ n \\ n Verifique os valores de entrada que inseriu (como datas) para este relatório e tente novamente.");
				mNoPages.open();
			}
								
		} catch (SQLException | ClassNotFoundException e) { 
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
}
