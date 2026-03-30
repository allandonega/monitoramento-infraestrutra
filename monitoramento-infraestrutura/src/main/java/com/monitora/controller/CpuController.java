package com.monitora.controller;

import com.monitora.dto.CpuInfoDTO;
import com.monitora.service.ColetorCpuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/cpu")
@RequiredArgsConstructor
public class CpuController {

    private final ColetorCpuService coletorCpuService;

    @GetMapping
    public String cpuDashboard(Model model) {
        CpuInfoDTO info = coletorCpuService.coletarDadosAtuais();
        model.addAttribute("cpu", info);
        model.addAttribute("paginaAtiva", "cpu");
        return "cpu";
    }

    @GetMapping("/api/current")
    @ResponseBody
    public CpuInfoDTO getCurrentCpu() {
        return coletorCpuService.coletarDadosAtuais();
    }
}
