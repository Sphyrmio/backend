package com.eurasia.pro_skill.application;

import com.eurasia.pro_skill.domain.InboundOrder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

// 作业一：开关关掉时，同一张入库单被同时完成两次的结果。开关在这里写死，不读 yaml。
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "app.lesson.inbound-complete-once=off",
        "app.lesson.outbound-read-pause-ms=500"
})
@DisplayName("作业一：入库只能完成一次的开关关掉")
class InboundDefectPathTest extends OutboundRaceSupport {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private OutboundService outboundService;
    @Autowired
    private InboundService inboundService;

    @AfterEach
    void tearDown() {
        cleanUp();
    }

    @Test
    @DisplayName("同一张单同时完成两次：两次都完成，库存加了两遍，流水两行")
    void bothCompleteWithoutCondition() throws Exception {
        prepareStock(10);
        InboundOrder order = inboundOf(34);

        List<String> results = completeInboundTogether(order.getId(), order.getId());

        assertEquals(List.of("COMPLETED", "COMPLETED"), results);
        assertEquals(10 + 34 + 34, quantity()); // 只该加一次 34，实际加了两次
        assertEquals(2, flowCount(order.getOrderNo()));
        assertEquals("COMPLETED", inboundStatus(order.getId()));
    }

    @Override
    protected JdbcTemplate jdbc() {
        return jdbcTemplate;
    }

    @Override
    protected OutboundService outboundService() {
        return outboundService;
    }

    @Override
    protected InboundService inboundService() {
        return inboundService;
    }
}
