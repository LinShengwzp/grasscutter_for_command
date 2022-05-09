package com.anmi.grasscutter.modules.controller;

import com.anmi.grasscutter.modules.domain.dto.Res;
import com.anmi.grasscutter.modules.domain.vo.SendCommandVo;
import com.anmi.grasscutter.netty.client.MessageSend;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
public class CommandController {

    @Resource
    private MessageSend messageSend;
    @GetMapping("test")
    public Res test() {
        String s = messageSend.sendCommand("give @2020 223");
        return Res.err(s);
    }

    @PostMapping("command")
    public Res command(@RequestBody SendCommandVo commandVo) {
        messageSend.sendCommand(commandVo.getCommand());
        return Res.ok();
    }
}
