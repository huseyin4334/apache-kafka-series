# Topic Naming Strategy
`<message_type>.<dataset_name>.<data_name>.<data_format>`

- Message Type: `view`, `play`, `search`, `click`, `purchase`, `logging`, ...
- Dataset Name: `raw`, `clean`, `aggregated`, `summary`, ...
  - `raw`: Original data from source
  - `clean`: Cleaned data
- Data Name: `user`, `product`, `order`, `log`, ...
- Data Format: `json`, `csv`, `parquet`, `avro`, `orc`, `delta`, ...

Example:
- `view.raw.user.json`
- `play.raw.product.json`

> Use snake_case for naming strategy