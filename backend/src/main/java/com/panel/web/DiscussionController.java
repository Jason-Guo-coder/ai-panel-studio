package com.panel.web;

import com.panel.entity.Discussion;
import com.panel.service.DiscussionService;
import com.panel.web.dto.CreateDiscussionReq;
import com.panel.web.dto.DiscussionDetailDto;
import com.panel.web.dto.RosterResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/discussions")
public class DiscussionController {

    private final DiscussionService service;

    public DiscussionController(DiscussionService service) {
        this.service = service;
    }

    @GetMapping
    public List<Discussion> list() {
        return service.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RosterResponseDto create(@RequestBody CreateDiscussionReq req) {
        return service.createWithRoster(req.topic(), req.expertCount());
    }

    @PostMapping("/{id}/regenerate")
    public RosterResponseDto regenerate(@PathVariable long id) {
        return service.regenerate(id);
    }

    @GetMapping("/{id}")
    public DiscussionDetailDto detail(@PathVariable long id) {
        return service.detail(id);
    }
}
