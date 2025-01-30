package com.example.core.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
public class OrderApproveCommand {
    private UUID orderId;
}
