package br.ueg.tc.ueg_provider.serviceprovider;

import br.ueg.tc.pipa_integrator.annotations.ServiceProviderClass;
import br.ueg.tc.pipa_integrator.exceptions.GenericBusinessException;
import br.ueg.tc.pipa_integrator.interfaces.providers.IBaseInstitutionProvider;
import br.ueg.tc.pipa_integrator.interfaces.platform.IUser;
import br.ueg.tc.pipa_integrator.interfaces.providers.EmailDetails;
import br.ueg.tc.pipa_integrator.interfaces.providers.IPlataformService;
import br.ueg.tc.pipa_integrator.interfaces.providers.parameters.AParameter;
import br.ueg.tc.pipa_integrator.interfaces.providers.parameters.ParameterValue;
import br.ueg.tc.ueg_provider.UEGProvider;
import br.ueg.tc.ueg_provider.infos.UserDataUEG;
import br.ueg.tc.ueg_provider.serviceprovider.parameter.UserParameter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static br.ueg.tc.ueg_provider.enums.DocEnum.ATTENDANCE_DECLARATION;

@Service
@ServiceProviderClass(personas = {"Aluno"})
public class AttendanceDeclarationService extends InstitutionService {

    public AttendanceDeclarationService() {}
    public AttendanceDeclarationService(IUser user) {
        super(user);
    }

    public List<String> getActivationName() {
        return List.of("gerar declaração de frequência",
                "declaracao de frequencia",
                "declaração de frequencia",
                "declaração frequencia", "declaracao frequencia");
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
            String pdfPath = generateAttendanceDeclarationPDF(institution, plataformService);

            EmailDetails emailDetails = buildEmailDetails(studentData, pdfPath);

            return sendEmail(plataformService, emailDetails);
        } catch (RuntimeException e) {
            throw new GenericBusinessException("Houve um erro ao enviar sua declaração de frequência, tente novamente mais tarde");
        }

    }

    private UserDataUEG getStudentData(Set<ParameterValue> parameterValues) {
        return parameterValues.stream()
                .findFirst()
                .map(parameterValue -> (UserDataUEG) parameterValue.getValue())
                .orElseThrow(() -> new GenericBusinessException
                        ("Não foram encontrados os dados do estudante para envio da declaração"));
    }

    private void validateStudentEmail(UserDataUEG studentData) {
        if (Objects.isNull(studentData) ||
                Objects.isNull(studentData.getEmail()) || studentData.getEmail().isEmpty()) {
            throw new GenericBusinessException("Não foi encontrado o email do estudante para envio da declaração");
        }
    }

    private String generateAttendanceDeclarationPDF(IBaseInstitutionProvider institution, IPlataformService plataformService) {
        String attendanceDeclarationHTML = ((UEGProvider) institution).generateNewAttendanceDeclarationHTML();
        return plataformService.HTMLToPDF(attendanceDeclarationHTML, ATTENDANCE_DECLARATION.getFolderPath(),
                ATTENDANCE_DECLARATION.getFilePrefix());

    }

    private EmailDetails buildEmailDetails(UserDataUEG studentData, String pdfPath) {
        return new EmailDetails(studentData.getFirstName(), studentData.getEmail(),
                "DECLARAÇÃO DE FREQUENCIA UEG",
                "Olá, segue em anexo sua declaração de frequencia como estudante da UEG",
                "Declaracao_Frequencia", pdfPath);
    }

    private String sendEmail(IPlataformService plataformService, EmailDetails emailDetails) {
        if (plataformService.sendEmailWithFileAttachment(emailDetails)) {
            return "Sua declaração de frequência foi enviada para o seu e-mail acadêmico.";
        }
        return "Houve um erro ao enviar sua declaração de frequência, tente novamente mais tarde";
    }

}
