package br.ueg.tc.ueg_provider.serviceprovider;

import br.ueg.tc.apiai.service.AiService;
import br.ueg.tc.pipa_integrator.ai.AIClient;
import br.ueg.tc.pipa_integrator.annotations.ServiceProviderClass;
import br.ueg.tc.pipa_integrator.annotations.ServiceProviderMethod;
import br.ueg.tc.pipa_integrator.enums.WeekDay;
import br.ueg.tc.pipa_integrator.exceptions.GenericBusinessException;
import br.ueg.tc.pipa_integrator.exceptions.institution.InstitutionCommunicationException;
import br.ueg.tc.pipa_integrator.exceptions.institution.InstitutionServiceException;
import br.ueg.tc.pipa_integrator.exceptions.intent.IntentNotSupportedException;
import br.ueg.tc.pipa_integrator.exceptions.user.UserNotFoundException;
import br.ueg.tc.pipa_integrator.interfaces.platform.IUser;
import br.ueg.tc.pipa_integrator.interfaces.providers.EmailDetails;
import br.ueg.tc.pipa_integrator.interfaces.providers.IBaseInstitutionProvider;
import br.ueg.tc.pipa_integrator.interfaces.providers.IPlatformService;
import br.ueg.tc.pipa_integrator.interfaces.providers.info.IDisciplineGrade;
import br.ueg.tc.pipa_integrator.interfaces.providers.info.IUserData;
import br.ueg.tc.pipa_integrator.interfaces.providers.parameters.ParameterValue;
import br.ueg.tc.ueg_provider.UEGProvider;
import br.ueg.tc.ueg_provider.ai.AIApi;
import br.ueg.tc.ueg_provider.converter.ConverterUEG;
import br.ueg.tc.ueg_provider.dto.KeyUrl;
import br.ueg.tc.ueg_provider.formatter.Formatter;
import br.ueg.tc.ueg_provider.infos.ComplementaryActivityUEG;
import br.ueg.tc.ueg_provider.infos.ExtensionActivityUEG;
import br.ueg.tc.ueg_provider.infos.UserDataUEG;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static br.ueg.tc.ueg_provider.UEGEndpoint.*;
import static br.ueg.tc.ueg_provider.enums.DocEnum.ACADEMIC_RECORD;

@Service
@ServiceProviderClass(personas = {"Aluno"})
public class StudentService extends InstitutionService {

    @Autowired
    AiService<AIClient> aiService;
    @Autowired
    IPlatformService platformService;
    private String acuId;

    public StudentService() {
        super();
    }

    public StudentService(IUser user) {
        super(user);
    }

    private boolean responseOK(CloseableHttpResponse httpResponse) {
        return httpResponse.getCode() == 200;
    }


    public void getPersonId() {
        acuId = getUserData().getPersonId();
    }

    public IUserData getUserData() throws IntentNotSupportedException, InstitutionCommunicationException {
        HttpGet httpGet = new HttpGet(PERFIL);
        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet, localContext);
            HttpEntity entity = httpResponse.getEntity();
            if (responseOK(httpResponse)) {
                return converterUEG.getUserDataFromJson(JsonParser.
                        parseString(EntityUtils.toString(entity)));
            }
            throw new UserNotFoundException();
        } catch (Throwable error) {
            throw new InstitutionCommunicationException("Não foi possível se comunicar com o servidor da UEG," +
                    " tente novamente mais tarde");
        }
    }

    @ServiceProviderMethod(activationPhrases = {"Qual minha média geral",
            "média geral", "qual minha nota geral", "média", "qual a nota geral?"})
    public String getGeneralGrade() {
        getPersonId();
        HttpGet httpGet = new HttpGet(DADOS_ACADEMICOS);
        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();

            if (responseOK(httpResponse)) {
                String entityString = EntityUtils.toString(entity);
                if (entityString == null || entityString.isEmpty()) return null;

                String mediaGeral = extractFromJson(entityString, "media_geral");

                return mediaGeral != null ?
                        "Sua média geral é: " + mediaGeral :
                        "Não foi possível encontrar sua média geral no sistema.";
            } else {
                throw new InstitutionServiceException("Ocorreu um problema na obtenção da nota geral. Tente novamente mais tarde.");
            }

        } catch (Exception error) {
            throw new InstitutionCommunicationException("Não foi possível se comunicar com o servidor da UEG. Tente novamente mais tarde.");
        }
    }

    @ServiceProviderMethod(activationPhrases = {"Qual minha nota em matemática",
            "média geral em programação", "qual minha nota em portugues"})
    public List<IDisciplineGrade> getGradeByDiscipline(String discipline) {
        getPersonId();
        HttpGet httpGet = new HttpGet(DADOS_DISCIPLINAS + acuId);
        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();

            if (responseOK(httpResponse)) {
                String entityString = EntityUtils.toString(entity);
                Formatter formatter = new Formatter();
                if (entityString == null || entityString.isEmpty()) return null;
                discipline = getDisciplineNameResponse(discipline, entityString);
                return formatter.disciplineGradeByDisciplineName(discipline,
                        converterUEG.getGradesWithDetailedGradeFromJson((JsonArray) JsonParser.parseString(entityString)));

            } else {
                throw new InstitutionServiceException("Ocorreu um problema na obtenção da nota. Tente novamente mais tarde.");
            }

        } catch (Exception error) {
            throw new InstitutionCommunicationException("Não foi possível se comunicar com o servidor da UEG. Tente novamente mais tarde.");
        }
    }

    @ServiceProviderMethod(activationPhrases = {"Quais minhas notas do primeiro semestre",
            "notas do periodo 7", "3° periodo notas"})
    public String getGradesBySemester(String semester) {
        getPersonId();
        HttpGet httpGet = new HttpGet(DADOS_DISCIPLINAS + acuId);
        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();

            if (responseOK(httpResponse)) {
                String entityString = EntityUtils.toString(entity);
                Formatter formatter = new Formatter();
                if (entityString == null || entityString.isEmpty()) return null;
                return formatter.formatDisciplineGrade(formatter.disciplineGradeBySemester(semester,
                        converterUEG.getGradesWithDetailedGradeFromJson((JsonArray) JsonParser.parseString(entityString))));

            } else {
                throw new InstitutionServiceException("Ocorreu um problema na obtenção das notas. Tente novamente mais tarde.");
            }

        } catch (Exception error) {
            throw new InstitutionCommunicationException("Não foi possível se comunicar com o servidor da UEG. Tente novamente mais tarde.");
        }
    }


    @ServiceProviderMethod(activationPhrases = {"Quais minhas aulas?", "Aulas da semana", "Quais minhas aulas da semana", "Horário de aula"})
    public String getAllSchedule() throws IntentNotSupportedException {
        HttpGet httpGet = new HttpGet(HORARIO_AULA);
        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();

            if (responseOK(httpResponse)) {
                String entityString = EntityUtils.toString(entity);
                if (entityString == null || entityString.isEmpty()) return null;
                Formatter formatter = new Formatter();
                return formatter.formatSchedule(converterUEG.getDisciplinesWithScheduleFromJson
                        ((JsonArray) JsonParser.parseString(entityString)
                        ));
            } else
                throw new InstitutionCommunicationException("Ocorreu um problema na obtenção do horario, tente novamente mais tarde");

        } catch (Exception error) {
            throw new InstitutionCommunicationException("Ocorreu um problema na obtenção do horario," +
                    " tente novamente mais tarde");
        }
    }

    @ServiceProviderMethod(activationPhrases = {"Quais minhas aulas de segunda",
            "Aula de terça", "Aulas de Sábado", "Quais minhas aulas hoje", "Aulas de amanhã"})
    public String getScheduleByDay(String day) {
        HttpGet httpGet = new HttpGet(HORARIO_AULA);
        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();

            if (responseOK(httpResponse)) {
                String entityString = EntityUtils.toString(entity);
                if (entityString == null || entityString.isEmpty()) return null;
                Formatter formatter = new Formatter();
                day = getWeekByValue(day);
                if (Objects.equals(day, "NENHUMA")) {
                    return "Você não tem aulas nesse dia";
                }
                return formatter.formatSchedule(formatter.disciplinesWithScheduleByDay(WeekDay.getByShortName(day),
                        converterUEG.getDisciplinesWithScheduleFromJson
                                ((JsonArray) JsonParser.parseString(entityString)))
                );
            } else
                throw new InstitutionServiceException("Ocorreu um problema na obtenção do horario, tente novamente mais tarde");

        } catch (Exception error) {
            throw new InstitutionCommunicationException("Ocorreu um problema na obtenção do horario," +
                    " tente novamente mais tarde");
        }
    }


    @ServiceProviderMethod(activationPhrases = {"Quais minha aulas em matemática",
            "Aula de português", "Quando é a aula de Português",
            "Quando é minha aula de infra",
            "Quando é minha aula de INFRAESTRUTURA PARA SISTEMAS DE INFORMAÇÃO"})
    public String getScheduleByDisciplineName(String disciplineToGetSchedule) {
        HttpGet httpGet = new HttpGet(HORARIO_AULA);
        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            if (responseOK(httpResponse)) {
                String entityString = EntityUtils.toString(entity);
                if (entityString == null || entityString.isEmpty()) return null;
                disciplineToGetSchedule = getDisciplineNameResponse(disciplineToGetSchedule, entityString);
                if (Objects.equals(disciplineToGetSchedule, "NENHUMA")) {
                    return "Você não tem aulas dessa matéria";
                }
                Formatter formatter = new Formatter();
                return formatter.formatSchedule(formatter.scheduleByDisciplineName(disciplineToGetSchedule, converterUEG.getDisciplinesWithScheduleFromJson
                        ((JsonArray) JsonParser.parseString(entityString)))
                );
            } else
                throw new InstitutionServiceException("Ocorreu um problema na obtenção do horario, tente novamente mais tarde");

        } catch (Exception error) {
            throw new InstitutionCommunicationException("Ocorreu um problema na obtenção do horário, " +
                    "tente novamente mais tarde");
        }
    }

    @ServiceProviderMethod(activationPhrases = {
            "Ajuda",
            "O que você pode fazer",
            "O que posso fazer",
            "Funcionalidades"})
    public String getAllFunctionalities() {
        return """
                Como Aluno, você pode realizar as seguintes consultas:
                
                **Disciplinas e Notas**
                • Ver sua média geral.
                • Consultar notas por disciplina.
                • Consultar notas por período/semestre.
                • Ver disciplinas já concluídas.
                
                **Horários de Aula**
                • Ver todas as aulas da semana.
                • Consultar aulas por dia específico.
                • Ver quando tem aula de determinada disciplina.
                
                **Faltas**
                • Ver faltas por disciplina.
                
                **Atividades Complementares**
                • Consultar todas as atividades complementares com detalhes.
                • Ver um resumo das horas complementares realizadas.
                
                **Atividades de Extensão**
                • Ver as atividades de extensão cadastradas e suas cargas horárias.
                
                **Documentos Acadêmicos**
                • Solicitar o envio do Histórico Acadêmico para seu e-mail institucional.
                """;
    }

    @ServiceProviderMethod(activationPhrases = {"Quais minhas faltas em português", "Quais minhas faltas em matematica", "Faltas em biologia II"})
    public String getAbsencesByDiscipline(String discipline) {
        getPersonId();
        HttpGet httpGet = new HttpGet(DADOS_DISCIPLINAS + acuId);
        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            if (responseOK(httpResponse)) {
                String entityString = EntityUtils.toString(entity);
                if (entityString == null || entityString.isEmpty()) return null;
                discipline = getDisciplineNameResponse(discipline, entityString);
                if (Objects.equals(discipline, "NENHUMA")) {
                    return "Você não tem notas nessa matéria";
                }
                Formatter formatter = new Formatter();
                return formatter.formatAbsence(formatter.absencesByDisciplineName(discipline,
                        converterUEG.getDisciplinesWithAbsencesFromJson(
                                ((JsonArray) JsonParser.parseString(entityString)))
                ));
            } else
                throw new InstitutionServiceException("Não foi possível se comunicar com o servidor da UEG," +
                        " tente novamente mais tarde");

        } catch (Exception error) {
            throw new InstitutionCommunicationException("Não foi possível se comunicar com o servidor da UEG," +
                    " tente novamente mais tarde");
        }

    }

    @ServiceProviderMethod(activationPhrases = {"Quais materias ja fiz", "materias concluidas", "Disciplinas completas"})
    public String getCompletedCourses() {
        getPersonId();
        HttpGet httpGet = new HttpGet(DADOS_DISCIPLINAS + acuId);
        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            if (responseOK(httpResponse)) {
                String entityString = EntityUtils.toString(entity);
                if (entityString == null || entityString.isEmpty()) return null;
                Formatter formatter = new Formatter();
                return formatter.formatDiscipline(formatter.disciplineByStatus("aprovado",
                        converterUEG.getDisciplinesFromJson(
                                ((JsonArray) JsonParser.parseString(entityString)))
                ));
            } else
                throw new InstitutionServiceException("Ocorreu um problema na obtenção das matérias, tente novamente mais tarde");

        } catch (Exception error) {
            throw new InstitutionCommunicationException("Não foi possível se comunicar com o servidor da UEG," +
                    " tente novamente mais tarde");
        }

    }


    @ServiceProviderMethod(activationPhrases = {"Atividades complementares", "Como estão minhas atividades complementares", "detalhes das atividades complementares", "todas as atividades complementares"})
    public String getAllDetailedComplementaryActivities() {
        getPersonId();
        HttpGet httpGet = new HttpGet(DADOS_ATV_COMPLEMENTARES + acuId + "&page=1&rows_limit=1000&sort_by=&descending=f");
        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            if (responseOK(httpResponse)) {
                String entityString = EntityUtils.toString(entity);
                if (entityString == null || entityString.isEmpty()) return null;
                Formatter formatter = new Formatter();
                return formatter.formatComplementaryActivities(
                        converterUEG.getComplementaryActivitiesFromJson(
                                (JsonParser.parseString(entityString)))
                );
            } else
                throw new InstitutionServiceException("Ocorreu um problema na obtenção das atividades complementares, tente novamente mais tarde");

        } catch (Exception error) {
            throw new InstitutionCommunicationException("Não foi possível se comunicar com o servidor da UEG," +
                    " tente novamente mais tarde");
        }

    }

    @ServiceProviderMethod(activationPhrases = {"Resumo da atividades complementares", "Resuma como estão minhas atividades complementares", "status das atividades complementares", "resumo atv complementares"})
    public String getDigestComplementaryActivities() {
        getPersonId();
        HttpGet httpGet = new HttpGet(DADOS_ATV_COMPLEMENTARES_HORAS + acuId);
        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            if (responseOK(httpResponse)) {
                String entityString = EntityUtils.toString(entity);
                if (entityString == null || entityString.isEmpty()) return null;
                ComplementaryActivityUEG complementaryActivity = converterUEG.getComplementaryHoursActivitiesFromJson(
                        (JsonParser.parseString(entityString)));
                Formatter formatter = new Formatter();
                return formatter.formatComplementaryActivities(complementaryActivity);
            } else
                throw new InstitutionServiceException("Ocorreu um problema na obtenção do resumo das atividades complementares, tente novamente mais tarde");

        } catch (Exception error) {
            throw new InstitutionCommunicationException("Não foi possível se comunicar com o servidor da UEG," +
                    " tente novamente mais tarde");
        }

    }

    @ServiceProviderMethod(activationPhrases = {"Status das horas de extensão", "Resuma como estão minhas atividades de extensão", "status das atividades complementares", "Horas de extensão"})
    public String getAllExtensionActivities() {
        getPersonId();
        HttpGet httpGet = new HttpGet(DADOS_ATV_EXTENSAO + acuId);
        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            if (responseOK(httpResponse)) {
                String entityString = EntityUtils.toString(entity);
                if (entityString == null || entityString.isEmpty()) return null;
                List<ExtensionActivityUEG> extensionActivities = converterUEG.getExtensionActivityFromJson(
                        (JsonArray) (JsonParser.parseString(entityString)));
                Formatter formatter = new Formatter();
                return formatter.formatExtensionActivities(extensionActivities);
            } else
                throw new InstitutionServiceException("Ocorreu um problema na obtenção das atividades de extensão, tente novamente mais tarde" +
                        " tente novamente mais tarde");

        } catch (Exception error) {
            throw new InstitutionCommunicationException("Não foi possível se comunicar com o servidor da UEG," +
                    " tente novamente mais tarde");
        }

    }

    @ServiceProviderMethod(activationPhrases = {
            "Me mande a declaração de frequencia",
            "enviar declaração de frequencia",
            "declaração de frequencia",
            "declaracao de freq"
    })
    public String sendFrequencyDeclaration() {
        try {
            getPersonId();

            UserDataUEG studentData = (UserDataUEG) getUserData();

            validateStudentEmail(studentData.getEmail());

            String pdfPath = generateAcademicRecordPDF(platformService);

            EmailDetails emailDetails = buildEmailFrequencyDetails(studentData, pdfPath);

            return sendEmail(platformService, emailDetails);

        } catch (Exception e) {
            return "Ocorreu um erro ao enviar sua declaração de frequência: " + e.getMessage();
        }
    }


    //Métodos internos

    //TODO: Avaliar o impacto de mudar as keys de IA do provider,
    // tirando o uso da conta da pipa, mas deixando a API de IA pra universidade usar

    private String getDisciplineNameResponse(String discipline, String entityString) {
        discipline = aiService.sendPrompt(AIApi.startDisciplineNameQuestion + entityString + AIApi.endDisciplineNameQuestion + discipline);
        return discipline;
    }

    private String getWeekByValue(String weekDay) {
        return aiService.sendPrompt(AIApi.startWeekNameQuestion + LocalDateTime.now() + AIApi.endWeekNameQuestion + weekDay);
    }

    private UserDataUEG getStudentData(Set<ParameterValue> parameterValues) {
        return parameterValues.stream()
                .findFirst()
                .map(parameterValue -> (UserDataUEG) parameterValue.getValue())
                .orElseThrow(() -> new GenericBusinessException
                        ("Não foram encontrados os dados do estudante para envio do histórico acadêmico"));
    }

    private void validateStudentEmail(String studentEmail) {
        if (Objects.isNull(studentEmail) || studentEmail.isEmpty()) {
            throw new GenericBusinessException("Não foi encontrado o email do estudante para envio da histórico");
        }
    }

    private String generateAcademicRecordPDF(IPlatformService platformService) {
        String attendanceDeclarationHTML = generateNewAcademicRecordHTML();
        return platformService.HTMLToPDF(attendanceDeclarationHTML,
                ACADEMIC_RECORD.getFolderPath(),
                ACADEMIC_RECORD.getFilePrefix());

    }

    public String generateNewAcademicRecordHTML(){

            try {
                HttpPost httpPost = new HttpPost(GERAR_NOVA_DECLARACAO_FREQUENCIA);
                CloseableHttpResponse httpResponse = httpClient.execute(httpPost, localContext);
                HttpEntity entity = httpResponse.getEntity();

                if (responseOK(httpResponse)) {
                    KeyUrl keyUrl = ((ConverterUEG)converterUEG).getKeyUrlFromJson(
                            JsonParser.parseString(EntityUtils.toString(entity)));

                    if (Objects.nonNull(keyUrl) && !keyUrl.url().isEmpty()) {
                        return getHTMLFromURL(keyUrl.url());
                    }
                }
                throw new InstitutionServiceException("Não foi possível gerar sua declaração de frequencia," +
                        " tente novamente mais tarde");
            } catch (Throwable error) {
                throw new InstitutionCommunicationException("Não foi possivel se comunicar com o servidor da UEG," +
                        " tente novamente mais tarde");
            }

    }

    private String getHTMLFromURL(String url) {
        HttpGet httpGet = new HttpGet(url);
        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet, localContext);
            HttpEntity entity = httpResponse.getEntity();
            if (responseOK(httpResponse)) {
                return EntityUtils.toString(entity);
            }
            throw new InstitutionServiceException();
        } catch (Throwable error) {
            throw new InstitutionCommunicationException("Não foi possivel se comunicar com o servidor da UEG," +
                    " tente novamente mais tarde");
        }
    }

    private EmailDetails buildEmailHistoryDetails(UserDataUEG studentData, String pdfPath) {
        return new EmailDetails(studentData.getFirstName(), studentData.getEmail(),
                "HISTÓRICO ACADÊMICO UEG",
                "Olá, segue em anexo seu Histórico Acadêmico da UEG",
                "Histórico_Acadêmico", pdfPath);
    }

    private EmailDetails buildEmailFrequencyDetails(UserDataUEG studentData, String pdfPath) {
        return new EmailDetails(studentData.getFirstName(), studentData.getEmail(),
                "Declaração de Frequência UEG",
                "Olá, segue em anexo sua Declaração de frequência da UEG",
                "Declaração_Frequência", pdfPath);
    }

    private EmailDetails buildEmailBondDetails(UserDataUEG studentData, String pdfPath) {
        return new EmailDetails(studentData.getFirstName(), studentData.getEmail(),
                "HISTÓRICO ACADÊMICO UEG",
                "Olá, segue em anexo sua Declaração de vínculo da UEG",
                "Declaração_Vínculo", pdfPath);
    }

    private String sendEmail(IPlatformService platformService, EmailDetails emailDetails) {
        if (platformService.sendEmailWithFileAttachment(emailDetails)) {
            return "O documento foi enviado para o seu e-mail acadêmico.";
        }
        return "Houve um erro ao enviar seu documento, tente novamente mais tarde";
    }

}
