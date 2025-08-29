package org.plasma.order.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MetricsController {
    @Autowired
    private MetricsEndpoint metricsEndpoint;

    @GetMapping("/metrics")
    public String showMetrics(Model model) {
        model.addAttribute("metrics", metricsEndpoint.listNames().getNames());
        return "metrics";
    }
}