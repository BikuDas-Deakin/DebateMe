# DebateMe 🧠

> An on-device AI debate partner for Android — powered by Llama 3.2

## Overview

DebateMe is an Android application that pairs users with an AI debate partner running entirely on-device. The user states any opinion, selects a debate tone, and Llama 3.2 argues the opposing side in a real back-and-forth conversation — with zero cloud dependency and full privacy.

## Problem Statement

In an era of echo chambers and passive content consumption, most people rarely practise articulating and defending their views. Existing debate tools either require internet connectivity, lack conversational depth, or are built for classrooms rather than individuals.

## Proposed Solution

DebateMe puts an intelligent, private debate partner in your pocket — available offline, anytime.

## Features (Planned)

- 💬 **Topic Input** — Enter any opinion or claim to debate
- 🎭 **Tone Selector** — Choose between Casual, Academic, or Challenging debate styles
- 🤖 **Live Debate Chat** — Multi-turn back-and-forth with Llama 3.2
- 📚 **Session History** — Revisit and continue past debates locally

## Tech Stack

| Category | Technology |
|---|---|
| IDE | Android Studio |
| Language | Java 17 |
| AI Model | Llama 3.2 3B (INT4 quantised) |
| AI Inference | Google AI Edge SDK + MediaPipe LLM Inference API |
| Architecture | MVVM (ViewModel + LiveData) |
| Local Storage | Room Database (SQLite) |
| Deployment | On-Device Only — no cloud, no internet required |

## Architecture

```
UI Layer → ViewModel / Logic → Llama 3.2 (On-Device) → Room DB
```

- **UI Layer** — Activities, Fragments, RecyclerView chat interface
- **ViewModel** — PromptBuilder, TurnManager, Repository pattern
- **Llama 3.2** — MediaPipe LLM Inference API, on-device inference
- **Room DB** — Encrypted local storage for session history

## Project Status

> 📋 This repository is currently in the **proposal phase** (SIT708 HD Task 4.0).
> Development will begin following tutor approval.

## Subject

Deakin University — SIT708 Mobile Application Development
