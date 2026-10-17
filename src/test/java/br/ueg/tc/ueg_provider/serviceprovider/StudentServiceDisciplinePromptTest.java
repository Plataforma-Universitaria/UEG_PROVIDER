package br.ueg.tc.ueg_provider.serviceprovider;

import br.ueg.tc.pipa_integrator.ai.AiInferenceClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class StudentServiceDisciplinePromptTest {
    @Test
    void absencesAlwaysAskTheModelWithNamesAndUseRealAbsenceData() throws Exception {
        StudentService service = new StudentService() {
            @Override public void getPersonId() { }
        };
        AiInferenceClient inference = mock(AiInferenceClient.class);
        CloseableHttpClient http = mock(CloseableHttpClient.class);
        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        inject(service, StudentService.class, "aiInferenceClient", inference);
        inject(service, InstitutionService.class, "httpClient", http);
        when(http.execute(any(HttpGet.class))).thenReturn(response);
        when(response.getCode()).thenReturn(200);
        String json = """
                [{"disc_cursada":"ALGORITMOS I","mat_nfaltas":3,
                  "mat_nfaltas_abonadas":0,"mat_pfreq_aula_ministrada":"90",
                  "nota_list":[{"detalhe":"DADO_IRRELEVANTE_PARA_O_NOME"}]},
                 {"disc_cursada":"ALGORITMOS II","mat_nfaltas":7}]
                """;
        when(response.getEntity()).thenAnswer(ignored -> new StringEntity(json, ContentType.APPLICATION_JSON));
        when(inference.complete(anyString())).thenReturn("ALGORITMOS I");

        assertTrue(service.getAbsencesByDiscipline("alg 1").contains("Total de Faltas: 3"));
        assertTrue(service.getAbsencesByDiscipline("ALGORITMOS I").contains("Total de Faltas: 3"));
        ArgumentCaptor<String> prompts = ArgumentCaptor.forClass(String.class);
        verify(inference, times(2)).complete(prompts.capture());
        for (String prompt : prompts.getAllValues()) {
            assertTrue(prompt.contains("ALGORITMOS I\nALGORITMOS II\n"));
            assertFalse(prompt.contains("DADO_IRRELEVANTE_PARA_O_NOME"));
            assertFalse(prompt.contains("mat_nfaltas"));
        }
        assertTrue(prompts.getAllValues().getFirst().endsWith("Entrada: alg 1"));
        assertTrue(prompts.getAllValues().getLast().endsWith("Entrada: ALGORITMOS I"));
    }

    @Test
    void scheduleUsesItsOwnNameFieldAndStillAsksTheModel() throws Exception {
        StudentService service = new StudentService();
        AiInferenceClient inference = mock(AiInferenceClient.class);
        CloseableHttpClient http = mock(CloseableHttpClient.class);
        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        inject(service, StudentService.class, "aiInferenceClient", inference);
        inject(service, InstitutionService.class, "httpClient", http);
        when(http.execute(any(HttpGet.class))).thenReturn(response);
        when(response.getCode()).thenReturn(200);
        when(response.getEntity()).thenReturn(new StringEntity("""
                [{"disciplina":"ALGORITMOS I","horario":[],"turma":"DETALHE_FORA_DA_TAREFA_DE_NOME"},
                 {"disciplina":"ALGORITMOS II","horario":[]}]
                """, ContentType.APPLICATION_JSON));
        when(inference.complete(anyString())).thenReturn("NENHUMA");

        assertEquals("Você não tem aulas dessa matéria",
                service.getScheduleByDisciplineName("ALGORITMOS I"));
        ArgumentCaptor<String> prompt = ArgumentCaptor.forClass(String.class);
        verify(inference).complete(prompt.capture());
        assertTrue(prompt.getValue().contains("ALGORITMOS I\nALGORITMOS II\n"));
        assertFalse(prompt.getValue().contains("DETALHE_FORA_DA_TAREFA_DE_NOME"));
        assertTrue(prompt.getValue().endsWith("Entrada: ALGORITMOS I"));
    }

    private void inject(Object target, Class<?> owner, String name, Object value) throws Exception {
        Field field = owner.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
