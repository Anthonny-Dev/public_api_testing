package com.validation_api.validation;

import com.validation_api.validation.service.AggregationService;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class AggregationServiceTest {

    @Test
    public void testAgregacao() throws Exception {
        AggregationService service = new AggregationService();
        Path csvEnriquecido = Paths.get("src/main/resources/output/despesas_agregadas");
        service.agregarDados(csvEnriquecido);
    }
}
