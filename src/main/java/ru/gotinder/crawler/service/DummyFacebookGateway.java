package ru.gotinder.crawler.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import ru.gotinder.crawler.common.IFacebookService;

@ConditionalOnProperty(value = "tinder.crawler.fb.enabled", havingValue = "false")
@Service
public class DummyFacebookGateway implements IFacebookService {
}
