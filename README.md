# ShadowTraffic Predict

This is an open source extension for [ShadowTraffic](https://shadowtraffic.io/) to dynamically create configurations from running Kafka and Postgres instances.

This library is highly experimental, and intended to be forked to meet your needs. When you fork it, you may wish to change the LLM, AI prompt, and deserialization code to meet your needs.

PRs are welcome. See below for how to use it.

## Usage

1. Install Clojure

This library is written in Clojure, [make sure you have it](https://clojure.org/guides/install_clojure).

2. Install Ollama and download Mistal

Some of the configuration code is created with AI. I wanted this to work without requiring external services, so it's currently configured to use Ollama and Mistral.

[Download Ollama](https://ollama.com/), then install the Mistral Instruct model:

```
ollama run mistral:7b-instruct
```

Start the Ollama server:

```
ollama serve
```

3. Modify the configs

Lastly, swap out your configs at the bottom of `src/clj/io/shadowtraffic/predict/kafka.clj` or other. Modify any code you need to consume your data.

When you run it, you should see a ShadowTraffic generator configuration like what's listed below. You can feed this directly back into ShadowTraffic to generate synthetic data like it:

```json
{
  "generators" : [ {
    "topic" : "customers",
    "value" : {
      "activationDate" : {
        "_gen" : "formatDateTime",
        "ms" : {
          "_gen" : "now"
        }
      },
      "shippingAddress" : {
        "_gen" : "string",
        "expr" : "#{Address.fullAddress}"
      },
      "membershipLevel" : {
        "_gen" : "string",
        "expr" : "#{Job.seniority}"
      },
      "directSubscription" : {
        "_gen" : "boolean"
      },
      "birthday" : {
        "_gen" : "formatDateTime",
        "ms" : {
          "_gen" : "now"
        }
      },
      "name" : {
        "_gen" : "string",
        "expr" : "#{Name.fullName}"
      },
      "customerId" : {
        "_gen" : "uuid"
      }
    }
  }, {
    "topic" : "orders",
    "value" : {
      "timestamp" : {
        "_gen" : "normalDistribution",
        "mean" : 1.711658103657118E12,
        "sd" : 68.13148960467181,
        "decimals" : 0
      },
      "customerId" : {
        "_gen" : "uuid"
      },
      "product" : {
        "_gen" : "string",
        "expr" : "#{Commerce.productName}"
      },
      "cost" : {
        "_gen" : "normalDistribution",
        "mean" : 101.65896242453586,
        "sd" : 19.22403354299823
      },
      "creditCardNumber" : {
        "_gen" : "string",
        "expr" : "#{Business.creditCardNumber}"
      },
      "backordered" : {
        "_gen" : "boolean"
      },
      "orderNumber" : {
        "_gen" : "normalDistribution",
        "mean" : 249.5,
        "sd" : 144.4818327679989,
        "decimals" : 0
      },
      "orderId" : {
        "_gen" : "uuid"
      },
      "discountPercent" : {
        "_gen" : "normalDistribution",
        "mean" : 4.476,
        "sd" : 2.896584956257564,
        "decimals" : 0
      },
      "description" : {
        "_gen" : "string",
        "expr" : "#{Lorem.sentences}"
      }
    }
  } ]
}
```

## License

Copyright © 2024 ShadowTraffic

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
