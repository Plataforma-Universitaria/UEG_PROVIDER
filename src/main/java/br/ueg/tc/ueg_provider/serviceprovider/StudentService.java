package br.ueg.tc.ueg_provider.serviceprovider;

import br.ueg.tc.apiai.service.AiService;
import br.ueg.tc.pipa_integrator.ai.AIClient;
import br.ueg.tc.pipa_integrator.annotations.ServiceProviderClass;
import br.ueg.tc.pipa_integrator.annotations.ServiceProviderMethod;
import br.ueg.tc.pipa_integrator.enums.WeekDay;
import br.ueg.tc.pipa_integrator.exceptions.GenericBusinessException;
import br.ueg.tc.pipa_integrator.exceptions.institution.InstitutionComunicationException;
import br.ueg.tc.pipa_integrator.exceptions.intent.IntentNotSupportedException;
import br.ueg.tc.pipa_integrator.exceptions.user.UserNotFoundException;
import br.ueg.tc.pipa_integrator.interfaces.providers.IBaseInstitutionProvider;
import br.ueg.tc.pipa_integrator.interfaces.platform.IUser;
import br.ueg.tc.pipa_integrator.interfaces.providers.info.IDisciplineGrade;
import br.ueg.tc.pipa_integrator.interfaces.providers.info.IDisciplineSchedule;
import br.ueg.tc.pipa_integrator.interfaces.providers.info.ISchedule;
import br.ueg.tc.pipa_integrator.interfaces.providers.info.IUserData;
import br.ueg.tc.pipa_integrator.interfaces.providers.EmailDetails;
import br.ueg.tc.pipa_integrator.interfaces.providers.IPlataformService;
import br.ueg.tc.pipa_integrator.interfaces.providers.parameters.ParameterValue;
import br.ueg.tc.ueg_provider.UEGProvider;
import br.ueg.tc.ueg_provider.ai.AIApi;
import br.ueg.tc.ueg_provider.formatter.FormatterGradeByDisciplineName;
import br.ueg.tc.ueg_provider.formatter.FormatterScheduleByDisciplineName;
import br.ueg.tc.ueg_provider.formatter.FormatterScheduleByWeekDay;
import br.ueg.tc.ueg_provider.infos.UserDataUEG;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static br.ueg.tc.ueg_provider.UEGEndpoint.*;
import static br.ueg.tc.ueg_provider.enums.DocEnum.ACADEMIC_RECORD;

@Service
@ServiceProviderClass(personas = {"Aluno"})
public class StudentService extends InstitutionService {

    @Autowired
    AiService<AIClient> aiService;
    private String acuId;

    public StudentService() {
        super();
    }
    public StudentService(IUser user){
        super(user);
    }

    private boolean responseOK(CloseableHttpResponse httpResponse) {
        return httpResponse.getCode() == 200;
    }


    public void getPersonId() {
        acuId = getUserData().getPersonId();
    }

    public IUserData getUserData() throws IntentNotSupportedException, InstitutionComunicationException {
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
            throw new InstitutionComunicationException("Não foi possivel se comunicar com o servidor da UEG," +
                    " tente novamente mais tarde");
        }
    }

    @ServiceProviderMethod(activationPhrases = {"Qual minha média geral",
            "média geral", "qual minha nota geral", "qual a nota geral?"})
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
                throw new InstitutionComunicationException("Não foi possível se comunicar com o servidor da UEG. Tente novamente mais tarde.");
            }

        } catch (Exception error) {
            throw new InstitutionComunicationException("Ocorreu um problema na obtenção da nota geral. Tente novamente mais tarde.");
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
                FormatterGradeByDisciplineName formatter = new FormatterGradeByDisciplineName();
                if (entityString == null || entityString.isEmpty()) return null;
                discipline = getDisciplineNameResponse(discipline, entityString);
                return formatter.scheduleByDisciplineName(discipline,
                        converterUEG.getGradesWithDetailedGradeFromJson((JsonArray) JsonParser.parseString(entityString)));

            } else {
                throw new InstitutionComunicationException("Não foi possível se comunicar com o servidor da UEG. Tente novamente mais tarde.");
            }

        } catch (Exception error) {
            throw new InstitutionComunicationException("Ocorreu um problema na obtenção da nota geral. Tente novamente mais tarde.");
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
                return humanizeSchedule(converterUEG.getDisciplinesWithScheduleFromJson
                        ((JsonArray) JsonParser.parseString(entityString)
                        ));
            } else
                throw new InstitutionComunicationException("Não foi possivel se comunicar com o servidor da UEG, " +
                        "tente novamente mais tarde");

        } catch (Exception error) {
            throw new InstitutionComunicationException("Ocorreu um problema na obtenção do horario," +
                    " tente novamente mais tarde");
        }
    }

    @ServiceProviderMethod(activationPhrases = {"Quais minhas aulas de segunda",
            "Aula de terça", "Aulas de Sábado", "Quais minhas aulas hoje", "Aulas de amanhã"})
    public List<IDisciplineSchedule> getScheduleByDay(String day){
        HttpGet httpGet = new HttpGet(HORARIO_AULA);
        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();

            if (responseOK(httpResponse)) {
                String entityString = EntityUtils.toString(entity);
                if (entityString == null || entityString.isEmpty()) return null;
                FormatterScheduleByWeekDay formatter = new FormatterScheduleByWeekDay();
                day = getWeekByValue(day);
                return formatter.disciplinesWithScheduleByDay(WeekDay.getByShortName(day),
                        converterUEG.getDisciplinesWithScheduleFromJson
                                ((JsonArray) JsonParser.parseString(entityString))
                );
            } else
                throw new InstitutionComunicationException("Não foi possivel se comunicar com o servidor da UEG," +
                        " tente novamente mais tarde");

        } catch (Exception error) {
            throw new InstitutionComunicationException("Ocorreu um problema na obtenção do horario," +
                    " tente novamente mais tarde");
        }
    }


    @ServiceProviderMethod(activationPhrases = {"Quais minha aulas em matemática",
            "Aula de português", "Quando é a aula de Português",
            "Quando é minha aula de infra",
            "Quando é minha aula de INFRAESTRUTURA PARA SISTEMAS DE INFORMAÇÃO"})
    public List<IDisciplineSchedule> getScheduleByDisciplineName(String disciplineToGetSchedule){
        HttpGet httpGet = new HttpGet(HORARIO_AULA);
        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            if (responseOK(httpResponse)) {
                String entityString = EntityUtils.toString(entity);
                if (entityString == null || entityString.isEmpty()) return null;
                disciplineToGetSchedule = getDisciplineNameResponse(disciplineToGetSchedule, entityString);
                FormatterScheduleByDisciplineName formatter = new FormatterScheduleByDisciplineName();
                return formatter.scheduleByDisciplineName(disciplineToGetSchedule, converterUEG.getDisciplinesWithScheduleFromJson
                        ((JsonArray) JsonParser.parseString(entityString))
                );
            } else
                throw new InstitutionComunicationException("Não foi possivel se comunicar com o servidor da UEG," +
                        " tente novamente mais tarde");

        } catch (Exception error) {
            throw new InstitutionComunicationException("Ocorreu um problema na obtenção do horário, " +
                    "tente novamente mais tarde");
        }
    }

    private String getDisciplineNameResponse(String discipline, String entityString) {
      discipline = aiService.sendPrompt(AIApi.startDisciplineNameQuestion + entityString + AIApi.endDisciplineNameQuestion + discipline);
        return discipline;
    }

    private String getWeekByValue(String weekDay) {
        return aiService.sendPrompt(AIApi.startWeekNameQuestion + LocalDateTime.now() + AIApi.endWeekNameQuestion + weekDay);
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


    public String humanizeSchedule(List<IDisciplineSchedule> disciplinas) {
        StringBuilder resultado = new StringBuilder();
        DateTimeFormatter timeParser = DateTimeFormatter.ofPattern("HH:mm:ss");
        DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("HH:mm");
        LocalDate dataPadrao = LocalDate.now(); // Pode ser qualquer data válida

        for (IDisciplineSchedule disciplina : disciplinas) {
            Map<String, List<ISchedule>> horariosPorDia = new TreeMap<>();

            for (ISchedule s : disciplina.getScheduleList()) {
                String dia = WeekDay.getByShortName(s.getDay()).getFullName(); // Ex: "segunda-feira"
                horariosPorDia.computeIfAbsent(dia, k -> new ArrayList<>()).add(s);
            }

            for (Map.Entry<String, List<ISchedule>> entrada : horariosPorDia.entrySet()) {
                String diaSemana = entrada.getKey();
                List<ISchedule> horarios = entrada.getValue();

                // Parse para LocalDateTime com data fictícia
                horarios.sort(Comparator.comparing(h -> LocalDateTime.of(dataPadrao, LocalTime.parse(h.getStartTime(), timeParser))));

                LocalDateTime inicio = LocalDateTime.of(dataPadrao, LocalTime.parse(horarios.get(0).getStartTime(), timeParser));
                LocalDateTime fim = LocalDateTime.of(dataPadrao, LocalTime.parse(horarios.get(horarios.size() - 1).getEndTime(), timeParser));

                long intervaloTotal = 0;
                for (int i = 1; i < horarios.size(); i++) {
                    LocalDateTime fimAnterior = LocalDateTime.of(dataPadrao, LocalTime.parse(horarios.get(i - 1).getEndTime(), timeParser));
                    LocalDateTime inicioAtual = LocalDateTime.of(dataPadrao, LocalTime.parse(horarios.get(i).getStartTime(), timeParser));
                    long diff = Duration.between(fimAnterior, inicioAtual).toMinutes();
                    if (diff > 0) intervaloTotal += diff;
                }

                resultado.append(String.format(
                        "Na %s, você tem aula de %s, com %s, das %s às %s",
                        diaSemana,
                        disciplina.getDisciplineName(),
                        disciplina.getTeacherName() != null ? disciplina.getTeacherName().trim() : "professor não informado",
                        inicio.format(outputFormat),
                        fim.format(outputFormat)
                ));

                if (intervaloTotal > 0) {
                    resultado.append(String.format(" (Intervalo de %d minutos)", intervaloTotal));
                }

                resultado.append(".\n");
            }
        }

        return resultado.toString();
    }




}
