package br.ueg.tc.ueg_provider.serviceprovider;

import br.ueg.tc.apiai.service.AiService;
import br.ueg.tc.pipa_integrator.ai.AIClient;
import br.ueg.tc.pipa_integrator.annotations.ServiceProviderClass;
import br.ueg.tc.pipa_integrator.annotations.ServiceProviderMethod;
import br.ueg.tc.pipa_integrator.exceptions.institution.InstitutionCommunicationException;
import br.ueg.tc.pipa_integrator.exceptions.institution.InstitutionServiceException;
import br.ueg.tc.pipa_integrator.exceptions.user.UserNotFoundException;
import br.ueg.tc.pipa_integrator.interfaces.platform.IUser;
import br.ueg.tc.pipa_integrator.interfaces.providers.IEmailService;
import br.ueg.tc.ueg_provider.dto.TcDTO;
import br.ueg.tc.ueg_provider.formatter.Formatter;
import br.ueg.tc.ueg_provider.infos.DisciplineTeacherUEG;
import br.ueg.tc.ueg_provider.infos.TcDetailUEG;
import br.ueg.tc.ueg_provider.infos.UserDataTeacherUEG;
import br.ueg.tc.ueg_provider.services.UtilsService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static br.ueg.tc.ueg_provider.UEGEndpoint.*;

/**
 * Serviço para operações relacionadas ao professor (disciplinas, TCs, orientações).
 */
@Service
@ServiceProviderClass(personas = {"Professor"})
public class TeacherService extends InstitutionService {

    @Autowired
    private AiService<AIClient> aiService;

    @Autowired
    private IEmailService emailService;

    private String jwt;
    private UserDataTeacherUEG userDataTeacherUEG;

    public TeacherService() {
        super();
    }

    public TeacherService(IUser user) {
        super(user);
    }

    private void ensureAuthentication() {
        if (jwt == null) {
            jwt = getJwt().jwt();
        }
        if (userDataTeacherUEG == null) {
            loadUserData();
        }
    }

    private void loadUserData() {
        HttpGet httpGet = new HttpGet(DADOS_PROFESSOR);
        try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet, localContext)) {
            if (responseOK(httpResponse)) {
                String json = EntityUtils.toString(httpResponse.getEntity());
                userDataTeacherUEG = converterUEG.getUserDataTeacherFromJson(JsonParser.parseString(json));
            } else {
                throw new UserNotFoundException();
            }
        } catch (Exception e) {
            throw new InstitutionCommunicationException("Falha ao obter dados do professor na UEG.");
        }
    }

    private String executeGet(String url) {
        ensureAuthentication();
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("Authorization", "Bearer " + jwt);
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            if (responseOK(response)) {
                return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            }
            throw new InstitutionServiceException("Erro ao obter informações da UEG.");
        } catch (Exception e) {
            throw new InstitutionCommunicationException("Falha de comunicação com a UEG.");
        }
    }

    private String executePost(String url, List<NameValuePair> params) {
        ensureAuthentication();
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Authorization", "Bearer " + jwt);
        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        httpPost.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(httpPost)) {

            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            if (responseOK(response) && !responseBody.contains("Acesso Negado")) {
                return responseBody;
            }
            throw new InstitutionServiceException("Erro ao enviar dados: " + responseBody);
        } catch (Exception e) {
            throw new InstitutionCommunicationException("Erro de comunicação com a UEG.");
        }
    }


    @ServiceProviderMethod(
            activationPhrases = {"Listar materias q eu dou", "materias", "minhas matérias", "minhas disciplinas"},
            actionName = "Listar todas as disciplinas"
    )
    public String getAllDisciplines() {
        String json = executeGet(LISTAR_COMPONENTES +
                "?periodoIni=" + getPeriod() +
                "&periodoFim=" + getPeriod() +
                "&idPessoa=" + userDataTeacherUEG.getPersonId() +
                "&idCampus=" + userDataTeacherUEG.getDepId() +
                "&idCurso=" + getCourseId());

        List<DisciplineTeacherUEG> disciplines =
                converterUEG.getDisciplinesTeacherFromJson(JsonParser.parseString(json).getAsJsonArray());

        return new Formatter().formatDisciplineTeacher(disciplines);
    }

    public String getPeriod() {
        String json = executeGet(LISTAR_PERIODOS + userDataTeacherUEG.getDepId());
        JsonArray jsonArray = JsonParser.parseString(json).getAsJsonArray();
        return extractFromJson(String.valueOf(jsonArray.get(0)), "periodo").replace("\"", "");
    }

    public String getCourseId() {
        String json = executeGet(LISTAR_CURSOS_POR_PERIODOS + getPeriod());
        JsonArray jsonArray = JsonParser.parseString(json).getAsJsonArray();
        return extractFromJson(String.valueOf(jsonArray.get(0)), "id_curso").replace("\"", "");
    }

    @ServiceProviderMethod(activationPhrases = {
            "Registrar orientação de TCC para o aluno (student name) ex:Carlos, com carga de (hours)ex:2 e descrição: (description) ex:Avaliação de bibliografia selecionada no dia dd/mm(followupDate com formato dd/mm/aaaa)",
            "Registrar orientação de TCC: aluno (student name), descrição (description), duração (hours) horas",
            "Adicionar orientação: assunto (description), com (hours) horas, para o aluno (student name) (quando não tem a data o valor é a data de hoje)",
            "Nova orientação de TCC para (student name). Assunto: (description) (quando não tem as horas o valor é 1)",
            "Registrar orientação com o tema (description) para o aluno (student), com (hours) horas",
            "Orientação com descrição (description), para o aluno (student name) (quando não tem as horas o valor é 1)"
    },actionName = "Registrar nova orientação de TCC para um aluno onde deve ser dito o nome do estudante, as horas e descrição", manipulatesData = true)
    public String addOrientation(String studentName, String hours, String description, String followupDate) {
        String period = getPeriod();
        DisciplineTeacherUEG tc = findStudentTC(studentName, period);

        followupDate = followupDate.isBlank() ? UtilsService.getCurrentFormattedDate() : followupDate;
        hours = hours.isBlank() ? "1" : hours;

        TcDTO dto = new TcDTO(tc.getTcuId(), followupDate, hours, description, "", "");

        List<NameValuePair> params = List.of(
                new BasicNameValuePair("tcu_id", dto.tcuId()),
                new BasicNameValuePair("data_acompanhamento", dto.followupDate()),
                new BasicNameValuePair("horas", dto.hours()),
                new BasicNameValuePair("assuntos_discutidos", dto.details())
        );

        executePost(TC_ADICIONAR_ACOMPANHAMENTO, new ArrayList<>(params));

        return "Orientação de TCC registrada com sucesso para o aluno " + studentName +
                " por " + hours + " horas.";
    }

    @ServiceProviderMethod(
            activationPhrases = {
                    "Listar orientações do (nome do aluno)",
                    "Quais as orientações de TC da (nome do aluno)",
                    "Ver registros de orientação de TCC para (nome do aluno)",
                    "mostrar orientações para o aluno (nome do aluno)",
                    "orientações de TCC de (nome do aluno)"
            },
            actionName = "Listar todos os registros de orientação de TCC por aluno"
    )
    public String listOrientationsByStudent(String student) {
        List<TcDetailUEG> orientations = findOrientationsByStudent(student);
        if (orientations == null || orientations.isEmpty()) {
            return "Não encontrei registros de orientação para o aluno " + student + ".";
        }
        return "Aluno(a): " + student + "\n" + new Formatter().formatTCDetails(orientations);
    }

    private DisciplineTeacherUEG findStudentTC(String studentName, String period) {
        return listTCsByPeriod(period).stream()
                .filter(tc -> tc.getTcStudentName().contains(studentName.toUpperCase()))
                .findFirst()
                .orElseThrow(() -> new InstitutionServiceException("Não foi possível localizar o TC do aluno."));
    }

    private List<TcDetailUEG> findOrientationsByStudent(String student) {
        DisciplineTeacherUEG tc = findStudentTC(student, getPeriod());
        String json = executeGet(TC_BUSCAR_ACOMPANHAMENTO + tc.getTcuId());
        return converterUEG.getOrientationsFromJson(JsonParser.parseString(json).getAsJsonArray());
    }

    private List<DisciplineTeacherUEG> listTCsByPeriod(String period) {
        List<DisciplineTeacherUEG> list = getDisciplinesByPeriod(period);
        return list.stream().filter(tc -> Integer.parseInt(tc.getTcuId()) != 0).toList();
    }

    private List<DisciplineTeacherUEG> getDisciplinesByPeriod(String period) {
        String json = executeGet(LISTAR_COMPONENTES +
                "?periodoIni=" + period +
                "&periodoFim=" + period +
                "&idPessoa=" + userDataTeacherUEG.getPersonId() +
                "&idCampus=" + userDataTeacherUEG.getDepId() +
                "&idCurso=" + getCourseId());

        return converterUEG.getDisciplinesTeacherFromJson(JsonParser.parseString(json).getAsJsonArray());
    }
}
