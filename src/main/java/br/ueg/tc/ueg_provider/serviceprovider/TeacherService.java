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
import br.ueg.tc.ueg_provider.ai.AIApi;
import br.ueg.tc.ueg_provider.dto.TcDTO;
import br.ueg.tc.ueg_provider.formatter.Formatter;
import br.ueg.tc.ueg_provider.infos.CourseUEG;
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
import org.apache.hc.client5.http.utils.DateUtils;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static br.ueg.tc.ueg_provider.UEGEndpoint.*;
import static org.apache.hc.client5.http.utils.DateUtils.formatDate;

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
        ensureAuthentication();
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
            "Registrar orientação de TCC para o aluno (student name) ex:Felipe, com carga de (hours)ex:2 e descrição: (description) ex:Estruturação do sumário no dia (followupDate com formato dd/mm/aaaa) ex:15/10/2025",
            "Adicionar nova orientação",
            "Incluir orientação",
            "Registrar orientação de TC",
            "quero adicionar orientação",
            "Registrar orientação de TCC: aluno (student name) ex:Camila, descrição (description) ex:Análise de dados iniciais, duração (hours) ex:3 horas no dia (followupDate com formato dd/mm/aaaa) ex:22/10/2025",
            "Adicionar orientação: assunto (description) ex:Modelagem de dados, com (hours) ex:2 horas, para o aluno (student name) ex:Rafael (quando não tem a data o valor é a data de hoje)",
            "Adicione uma nova orientação para aluno Erick de 9/10/2025, com assunto: Avaliação de testes e conclçusão de ponto de entregas, carga horária 2 hora.",
            "Nova orientação de TCC para (student name) ex:Beatriz. Assunto: (description) ex:Definição de metodologia no dia (followupDate com formato dd/mm/aaaa) ex:29/10/2025 (quando não tem as horas o valor é 1)",
            "Registrar orientação com o tema (description) ex:Revisão de circuitos para o aluno (student) ex:Gustavo, com (hours) ex:1 horas no dia (followupDate com formato dd/mm/aaaa) ex:03/11/2025",
            "Orientação com descrição (description) ex:Implementação da funcionalidade X, para o aluno (student name) ex:Bruno no dia (followupDate com formato dd/mm/aaaa) ex:12/11/2025 (quando não tem as horas o valor é 1)"
    },actionName = "Registrar nova orientação de TCC para um aluno onde deve ser dito o nome do estudante, as horas e descrição e data",
            manipulatesData = true,
            addSpec = {"- A sequência de parâmetros na assinatura DEVE ser mantida e respeitada exatamente, sem inversões.\n" +
                    "- Cada parâmetro tem um tipo e domínio bem definidos. NÃO troque o significado entre eles.\n" +
                    "- Se o usuário mencionar dados em ordem diferente, reordene-os de acordo com a assinatura do método Java.",
                    "studentName: nome completo ou parcial do aluno vinculado ao TCC. Exemplo: 'Sara', 'Carlos Almeida'.",
                    "courseName: nome completo ou parcial do curso. Exemplo: 'Sistemas de Informação', 'Engenharia Civil'. Se não informado, será usado o primeiro curso do professor.",
                    "description: texto curto e livre descrevendo o conteúdo da orientação. Exemplo: 'Ajustes na metodologia', 'Revisão da introdução'.",
                    "hours: número inteiro (apenas um valor) representando a carga horária. Exemplo: '1', '2'. Caso não informado, assume '1'.",
                    "followupDate: data exata da orientação no formato 'dd/mm/aaaa'. Exemplo: '06/03/2002'."})
    public String addOrientation(String studentName, String courseName, String description, String hours, String followupDate) {
        Formatter formatter = new Formatter();
        String idCourse = isValidCourse(courseName);
        String period = getPeriod();
        DisciplineTeacherUEG tc = findStudentTC(studentName, period);

        followupDate = followupDate.isBlank() ? UtilsService.getCurrentFormattedDate() : followupDate;
        hours = hours.isBlank() ? "1" : hours;
        TcDetailUEG tcOrientation = findOrientationByTcuAndDate(tc.getTcuId(), formatter.normalizeDate(followupDate));
        if(Objects.nonNull(tcOrientation)) {
            return  "Já existe orientação nesta data.";
        }
        TcDTO dto = new TcDTO(tc.getTcuId(), followupDate, hours, description, "", "");

        List<NameValuePair> params = List.of(
                new BasicNameValuePair("tcu_id", dto.tcuId()),
                new BasicNameValuePair("data_acompanhamento", dto.followupDate()),
                new BasicNameValuePair("horas", dto.hours()),
                new BasicNameValuePair("assuntos_discutidos", dto.details())
        );

        executePost(TC_ADICIONAR_ACOMPANHAMENTO, new ArrayList<>(params));
        tcOrientation = findOrientationByTcuAndDate(tc.getTcuId(), formatter.normalizeDate(followupDate));
        if(Objects.nonNull(tcOrientation)) {
            return "Orientação de trabalho adicionada com sucesso!\n" +
                    "Aluno: " + tcOrientation.getStudent() +
                    "\nCurso: " + getCourseNameById(idCourse) +
                    "\nDescrição: " + description +
                    "\nCarga horária: " + hours;
        }
        return "Não consegui incluir sua orientação, pode ser mais específico?";

    }

    @ServiceProviderMethod(activationPhrases = {
            "Editar orientação de SI  do dia 06 (followupDate) de TCC para o aluno (student name) ex:Carlos, com carga de (hours)ex:2 e descrição: (description) ex:Avaliação de bibliografia selecionada",
            "Alterar orientação de Processamento de dados TCC: aluno (student name), descrição (description), duração (hours) horas do dia (followupDate)",
            "Alterar orientação do curso de Sistemas de informação: do dia (followupDate) assunto (description), com (hours) horas, para o aluno (student name)",
            "Editar no curso de si(courseName) orientação de TCC para (student name). Dia (followupDate) Assunto: (description) (quando não tem as horas o valor é 1)",
            "Atualizar curso de civil(courseName) orientação com o tema (description) para o aluno (student), com (hours) horas no dia (followupDate)",
            "Edite a orientação da sara(student name) de hoje de sistemas(course), colocando a descrição como :'Teste de esdição dos serviços dos professores com alterações'",
            "Alterar orientação com descrição (description) do curso de SI(course), para o aluno (student name) no dia (followupDate) (quando não tem as horas o valor é 1)"
    },
            actionName = "Editar o registro de orientação TCC",
            manipulatesData = true,
            addSpec = {"- A sequência de parâmetros na assinatura DEVE ser mantida e respeitada exatamente, sem inversões.\n" +
                    "- Cada parâmetro tem um tipo e domínio bem definidos. NÃO troque o significado entre eles.\n" +
                    "- Se o usuário mencionar dados em ordem diferente, reordene-os de acordo com a assinatura do método Java.",
                    "studentName: nome completo ou parcial do aluno vinculado ao TCC. Exemplo: 'Sara', 'Carlos Almeida'.",
                    "courseName: nome completo ou parcial do curso. Exemplo: 'Sistemas de Informação', 'Engenharia Civil'. Se não informado, será usado o primeiro curso do professor.",
                    "description: texto curto e livre descrevendo o conteúdo da orientação. Exemplo: 'Ajustes na metodologia', 'Revisão da introdução'.",
                    "hours: número inteiro (apenas um valor) representando a carga horária. Exemplo: '1', '2'. Caso não informado, assume '1'.",
                    "followupDate: converta a data exata da orientação para o formato 'aaaa-mm-dd'. Exemplo: '2025-10-06'."})
    public String editOrientation(String studentName, String courseName, String description, String hours, String followupDate) {
        String idCourse = isValidCourse(courseName);
        if(idCourse.equalsIgnoreCase("NENHUMA"))
            return "Não consegui encontrar essa informação.";
        String tcuId = isStudentNameValid(studentName, idCourse);

        if(followupDate.isBlank()) return "Não encontrei dados para o dia";

        hours = hours.isBlank() ? "1" : hours;
        if(followupDate.contains("/")){
            Formatter formatter = new Formatter();
            followupDate = formatter.normalizeDate(followupDate);
        }

        TcDetailUEG tcOrientation = findOrientationByTcuAndDate(tcuId, followupDate);
        if(Objects.isNull(tcOrientation))
            return "Não encontrei dados para está data, se quiser pode pedir por Adicionar uma nova orientação";

        List<NameValuePair> params = List.of(
                new BasicNameValuePair("tcp_id", tcOrientation.getTcpId()),
                new BasicNameValuePair("tcu_id", tcOrientation.getTcuId()),
                new BasicNameValuePair("data_acompanhamento", tcOrientation.getFollowupDate()),
                new BasicNameValuePair("horas", hours),
                new BasicNameValuePair("assuntos_discutidos", description),
                new BasicNameValuePair("parecer", tcOrientation.getEvaluation()),
                new BasicNameValuePair("status", tcOrientation.getStatus())
        );

        executePost(TC_ADICIONAR_ACOMPANHAMENTO, new ArrayList<>(params));

        tcOrientation = findOrientationByTcuAndDate(tcuId, followupDate);
        if(Objects.nonNull(tcOrientation)) {
            return "Orientação de trabalho atualizada com sucesso!\n" +
                    "Aluno: " + tcOrientation.getStudent() +
                    "\nCurso: " + getCourseNameById(idCourse) +
                    "\nDescrição: " + description +
                    "\nCarga horária: " + hours;
        }
        return "Não consegui atualizar sua orientação, pode ser mais específico?";

    }

    private String getCourseNameById(String idCourse){
        CourseUEG course =  converterUEG.getCoursesFromJson(JsonParser.parseString(getCoursesByPeriod(getPeriod())).getAsJsonArray()).stream().filter(courseUEG ->
                courseUEG.getCourseId().equalsIgnoreCase(idCourse)).findFirst().orElse(null);
        if(Objects.isNull(course))
            return idCourse;
        return Strings.toRootUpperCase(course.getCourseName() + "-" + course.getModality());
    }

    private TcDetailUEG findOrientationByTcuAndDate(String tcuId, String followupDate) {
        List<TcDetailUEG> orientations = findOrientationsByTcu(tcuId);

        if (orientations == null || orientations.isEmpty()) {
            return null;
        }
        orientations.forEach(orientation -> {
            orientation.setStudent(findTcByTcuId(tcuId).getTcStudentName());
        });
        return orientations.stream()
                .filter(o -> followupDate.equals(o.getFollowupDate()))
                .findFirst().orElse(null);

    }

    private List<TcDetailUEG> findOrientationsByTcu(String tcuId) {
        String json = executeGet(TC_BUSCAR_ACOMPANHAMENTO + tcuId);
        return converterUEG.getOrientationsFromJson(JsonParser.parseString(json).getAsJsonArray());
    }

    private DisciplineTeacherUEG findTcByTcuId(String tcuId) {
        return getTcsByPeriod(getPeriod()).stream().filter(tc -> tc.getTcuId().equalsIgnoreCase(tcuId)).findFirst().orElse(null);
    }

    private String isStudentNameValid(String studentName, String course) {
        String question =
                AIApi.studentNameQuestion +
                        getDisciplinesByPeriodAndCourseId(getPeriod(), course) +
                        "\n Entrada: " +
                        studentName;

        studentName = aiService.sendPrompt(question);
        return studentName;
    }


    private String isValidCourse(String course) {
        String question =
                AIApi.courseNameQuestion +
                        getCoursesByPeriod(getPeriod()) +
                        "\n Entrada: " +
                        course;

        course = aiService.sendPrompt(question);
        return course;
    }

    private String getCoursesByPeriod(String period) {
        String json = executeGet(LISTAR_CURSOS_POR_PERIODOS + getPeriod());
        return json;
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
        StringBuilder builder = new StringBuilder();
        if (orientations == null || orientations.isEmpty()) {
            return "Não encontrei registros de orientação para o aluno " + student + ".";
        }
        builder.append("Orientações do aluno(a): *")
                .append(student.trim())
                .append("*\n")
                .append("*-------------------------*\n")
                .append(new Formatter().formatTCDetails(orientations));

        return builder.toString();
    }

    @ServiceProviderMethod(
            activationPhrases = {
                    "Listar tcs de 20202",
                    "TCs (se não houver o período, o parâmetro é 0 )",
                    "Ver de TCC (se não houver o período, o parâmetro é 0 )",
                    "tcs q estou orientando (se não houver o período, o parâmetro é 0 )",
                    "os de trabalhos de curso  (se não houver o período, o parâmetro é 0 )"
            },
            actionName = "Listar todos os registros de orientação de TCC por aluno"
    )
    public String listTcsByPeriod(String period) {
        if (period == null || period.isEmpty() || period.equals("0")) {
            period = getPeriod();
        }
        List<DisciplineTeacherUEG> tcs = getTcsByPeriod(period);
        if (tcs.isEmpty()) {
            return "Não encontrei registros de tc para o peíodo " + period + ".";
        }
        return "Seus trabalhos de curso" + period + "\n" + new Formatter().formatDisciplineTeacher(tcs);
    }

    private @NotNull List<DisciplineTeacherUEG> getTcsByPeriod(String period) {
        List<DisciplineTeacherUEG> tcs = getDisciplinesByPeriod(period).stream().filter(tc -> !tc.getTcuId().equalsIgnoreCase("0")).toList();
        return tcs;
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
        loadUserData();
        String json = executeGet(LISTAR_COMPONENTES +
                "?periodoIni=" + period +
                "&periodoFim=" + period +
                "&idPessoa=" + userDataTeacherUEG.getPersonId() +
                "&idCampus=" + userDataTeacherUEG.getDepId() +
                "&idCurso=" + getCourseId());

        return converterUEG.getDisciplinesTeacherFromJson(JsonParser.parseString(json).getAsJsonArray());
    }

    private List<DisciplineTeacherUEG> getDisciplinesByPeriodAndCourseId(String period, String courseId) {
        String json = executeGet(LISTAR_COMPONENTES +
                "?periodoIni=" + period +
                "&periodoFim=" + period +
                "&idPessoa=" + userDataTeacherUEG.getPersonId() +
                "&idCampus=" + userDataTeacherUEG.getDepId() +
                "&idCurso=" + courseId);

        return converterUEG.getDisciplinesTeacherFromJson(JsonParser.parseString(json).getAsJsonArray());
    }
}
