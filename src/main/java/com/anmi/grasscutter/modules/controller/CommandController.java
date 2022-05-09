package com.anmi.grasscutter.modules.controller;

import com.anmi.grasscutter.modules.domain.dto.Res;
import com.anmi.grasscutter.modules.domain.vo.SendCommandVo;
import com.anmi.grasscutter.netty.client.MessageSend;
import org.springframework.web.bind.annotation.*;

@RestController
public class CommandController {
    @GetMapping("test")
    public Res test() {
        String s = MessageSend.sendCommand("give @2020 223");
        return Res.err(s);
    }

    @PostMapping("command")
    public Res command(@RequestBody SendCommandVo commandVo) {
        MessageSend.sendCommand(commandVo.getCommand());
        return Res.ok();
    }
}
