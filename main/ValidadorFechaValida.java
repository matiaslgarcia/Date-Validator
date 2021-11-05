package main;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ValidadorFechaValida {
        //Metodo que toma como parametro un LOCALDATE y retorna un Date
	public Date convertToDateViaSqlDate(LocalDate dateToConvert) {
		return java.sql.Date.valueOf(dateToConvert);
	}
        
        //Metodo que toma como parametro un DATE y retorna un LOCALDATE
	public LocalDate convertToLocalDateViaInstant(Date dateToConvert) {
		return dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}
	
        // Metodo que toma como parametro una fecha en formato DATE y devuelve una fecha en formato entero - Ej: 1994/11/24 --> 19941124
	public int fechaEventualInteger(Date fechaACalcular){ 
		LocalDate fechaLocal = this.convertToLocalDateViaInstant(fechaACalcular);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String fechaEventual = fechaLocal.format(formatter);
        int fechaEventualInt = Integer.parseInt(fechaEventual);
        
        return fechaEventualInt;
	}
	
        //Metodo que devuelve una lista de feriados Fijos
	public List<FeriadosFijos> listadoFeriadosFijos(){ 
            
                //METODO JPA (openXava) - Retorna una lsita de valores de una tabla en una base de detos que contiene FeriadosFijos
		List<FeriadosFijos> fj = (List<FeriadosFijos>) XPersistence.getManager().createQuery("from FeriadosFijos").getResultList();
		return fj;
	}
	
        //Metodo que devuelve una lista de feriados eventuales
	public List<FeriadosEventuales> listadoFeriadosEventuales(Date inicioGarantia){ 
		int fechaInicioGarantiaInt = this.fechaEventualInteger(inicioGarantia);
                
                //METODO JPA (openXava) - Retorna una lsita de valores de una tabla en una base de detos que contiene FeriadosEventuales
		List<FeriadosEventuales> fe = (List<FeriadosEventuales>) XPersistence.getManager()
				.createQuery("from FeriadosEventuales where gzfech >= '" + fechaInicioGarantiaInt + "'").getResultList();
		
		return fe;
	}
        
        // Metodo que devuelve una fecha valida controlando si es fin de semana , feriado eventual o feriado fijo
	public Date calculadorFechaValida (int salir ,Calendar calendar, Date fechaACalcular, int nroSucursal, Date inicioGarantia){ 
		while (salir == 0) {
			int fechaEventualInt = this.fechaEventualInteger(fechaACalcular);
			LocalDate fechaLocal = this.convertToLocalDateViaInstant(fechaACalcular);
			int monthACalcular = fechaLocal.getMonth().getValue();
			int dayACalcular = fechaLocal.getDayOfMonth();
			DayOfWeek dia = fechaLocal.getDayOfWeek();
			salir = 1;
			
			if (dia.equals(DayOfWeek.SATURDAY)) {
				calendar.add(Calendar.DATE, 2);
				salir = 0;
			} else if (dia.equals(DayOfWeek.SUNDAY)) {
				calendar.add(Calendar.DATE, 1);
				salir = 0;
			}
			for(FeriadosEventuales ferEventual: this.listadoFeriadosEventuales(inicioGarantia)){
				if (fechaEventualInt == ferEventual.getFechaEventual() && ferEventual.getNroSucursal() == nroSucursal){ 
					calendar.add(Calendar.DATE, 1);
					salir = 0;
				}
			}
			for(FeriadosFijos ferFijo: this.listadoFeriadosFijos()) {
				if(ferFijo.getMesFeriado() == monthACalcular && ferFijo.getDiaFeriado() == dayACalcular){ 
					calendar.add(Calendar.DATE, 1);
					salir = 0;
				}
			}
			fechaACalcular = calendar.getTime();
		}
		return fechaACalcular;
	}
}