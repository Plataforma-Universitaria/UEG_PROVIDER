package br.ueg.tc.ueg_provider.serviceprovider;


import br.ueg.tc.pipa_integrator.exceptions.BusinessException;
import br.ueg.tc.pipa_integrator.exceptions.GenericBusinessException;
import br.ueg.tc.pipa_integrator.institutions.IBaseInstitutionProvider;
import br.ueg.tc.pipa_integrator.institutions.info.IUserData;
import br.ueg.tc.pipa_integrator.plataformeservice.EmailDetails;
import br.ueg.tc.pipa_integrator.plataformeservice.IPlataformService;
import br.ueg.tc.pipa_integrator.serviceprovider.parameters.AParameter;
import br.ueg.tc.pipa_integrator.serviceprovider.parameters.ParameterValue;
import br.ueg.tc.pipa_integrator.serviceprovider.service.IServiceProvider;
import br.ueg.tc.ueg_provider.UEGProvider;
import br.ueg.tc.ueg_provider.infos.UserDataUEG;
import br.ueg.tc.ueg_provider.serviceprovider.parameter.UserParameter;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static br.ueg.tc.ueg_provider.enums.DocEnum.ACADEMIC_RECORD;

public class AcademicRecordService implements IServiceProvider {

    public List<String> getActivationName() {
        return List.of("gerar histórico acadêmico", "historico academico",
                "historico",
                "histórico acadêmico");
    }

    @Override
    public List<String> getValidPersonas() {
        return List.of("Aluno");
    }

    public List<AParameter> getParameters() {
        return List.of(new UserParameter());
    }

    public Set<ParameterValue> getParameterValues(IBaseInstitutionProvider institution, Map<String, String> parameters) {
        UserParameter userParameter = new UserParameter();
        ParameterValue parameterValue = new ParameterValue(userParameter,
                userParameter.getValueFromInstitution(institution));
        return Set.of(parameterValue);
    }

    public String doService(IBaseInstitutionProvider institution, Set<ParameterValue> parameterValues,
                            IPlataformService plataformService) {

        UserDataUEG studentData = getStudentData(parameterValues);

        validateStudentEmail(studentData);

        try{
            String pdfPath = generateAcademicRecordPDF(institution, plataformService);

            EmailDetails emailDetails = buildEmailDetails(studentData, pdfPath);

            return sendEmail(plataformService, emailDetails);
        } catch (RuntimeException e) {
            throw new GenericBusinessException("Houve um erro ao enviar seu histórico acadêmico, tente novamente mais tarde");
        }

    }

    private UserDataUEG getStudentData(Set<ParameterValue> parameterValues) {
        return parameterValues.stream()
                .findFirst()
                .map(parameterValue -> (UserDataUEG) parameterValue.getValue())
                .orElseThrow(() -> new GenericBusinessException
                        ("Não foram encontrados os dados do estudante para envio do histórico acadêmico"));
    }

    private void validateStudentEmail(UserDataUEG studentData) {
        if (Objects.isNull(studentData) ||
                Objects.isNull(studentData.getEmail()) || studentData.getEmail().isEmpty()) {
            throw new GenericBusinessException("Não foi encontrado o email do estudante para envio da histórico");
        }
    }

    private String generateAcademicRecordPDF(IBaseInstitutionProvider institution, IPlataformService plataformService) {
        String attendanceDeclarationHTML = ((UEGProvider) institution).generateNewAcademicRecordHTML();
        return plataformService.HTMLToPDF(attendanceDeclarationHTML, ACADEMIC_RECORD.getFolderPath(),
                ACADEMIC_RECORD.getFilePrefix());

    }

    private EmailDetails buildEmailDetails(UserDataUEG studentData, String pdfPath) {
        return new EmailDetails(studentData.getFirstName(), studentData.getEmail(),
                "HISTÓRICO ACADÊMICO UEG",
                "Olá, segue em anexo seu Histórico Acadêmico da UEG",
                "Histórico_Acadêmico", pdfPath);
    }

    private String sendEmail(IPlataformService plataformService, EmailDetails emailDetails) {
        if (plataformService.sendEmailWithFileAttachment(emailDetails)) {
            return "Seu Histórico Acadêmico foi enviado para o seu e-mail acadêmico.";
        }
        return "Houve um erro ao enviar seu Histórico Acadêmico, tente novamente mais tarde";
    }

    @Override
    public String doService(String activationPhrase, IUserData userData) throws BusinessException {
        return "Academic Service";
    }
}
