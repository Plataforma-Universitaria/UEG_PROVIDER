package br.ueg.tc.ueg_provider.serviceprovider;

import br.ueg.tc.pipa_integrator.enums.WeekDay;
import br.ueg.tc.pipa_integrator.exceptions.BusinessException;
import br.ueg.tc.pipa_integrator.institutions.info.IUserData;
import br.ueg.tc.pipa_integrator.serviceprovider.service.IServiceProvider;

import java.util.Date;
import java.util.List;

public class TeacherScheduleService implements IServiceProvider {
    @Override
    public String doService(String activationPhrase, IUserData userData) throws BusinessException {
        return "Deu certo";
    }

    @Override
    public List<String> getValidPersonas() {
        return List.of("Aluno");
    }

    @Override
    public Boolean isValidPersona(String persona) {
        return getValidPersonas().contains(persona);
    }

    @Override
    public Boolean manipulatesData() {
        return null;
    }

    public String getScheduleSByWeek(WeekDay weekDay) {
        return weekDay.toString();
    }

    public String getScheduleSByDay(Date date) {
        return date.toString();
    }
}
